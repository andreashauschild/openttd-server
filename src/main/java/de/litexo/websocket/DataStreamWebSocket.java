package de.litexo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.litexo.OpenttdProcess;
import de.litexo.ProcessThread;
import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalSnapshotEvent;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.security.SecurityService;
import de.litexo.services.OpenttdService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Live console of every OpenTTD process. The socket carries no credential in its handshake, so the first frame a
 * client sends must be a {@link SubscribeMessage} with the session id of a logged in admin. Until then the connection
 * receives nothing and it is closed after 'server.websocket.auth-timeout-ms'.
 * <p>
 * A subscription for a single process is answered with one {@link OpenttdTerminalSnapshotEvent} and only then with the
 * {@link OpenttdTerminalUpdateEvent}s of that process. Both carry absolute offsets, so a client appends every update
 * whose offset is not smaller than the end offset of the snapshot and has neither a gap nor a duplicate.
 */
@ServerEndpoint(value = "/data-stream", configurator = OriginCheckConfigurator.class)
@ApplicationScoped
public class DataStreamWebSocket {

    private static final Logger LOG = Logger.getLogger(DataStreamWebSocket.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final CloseReason UNAUTHORIZED = new CloseReason(CloseCodes.VIOLATED_POLICY, "unauthorized");

    /**
     * Time a connection gets to send its {@link SubscribeMessage}. Configurable so that the test does not have to wait
     * for the real timeout.
     */
    @ConfigProperty(name = "server.websocket.auth-timeout-ms", defaultValue = "5000")
    long authTimeoutMs;

    @Inject
    EventBus eventBus;

    @Inject
    SecurityService securityService;

    @Inject
    OpenttdService openttdService;

    /**
     * Connections that have not authenticated yet. A connection is either in here or in {@link #subscriptions}.
     */
    private final Map<String, Session> pending = new ConcurrentHashMap<>();

    Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    /**
     * Closes connections that never send their subscribe message. One thread is enough, the tasks only close a
     * session.
     */
    private ScheduledExecutorService authTimeoutExecutor;

    @PostConstruct
    void init() {
        this.authTimeoutExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "data-stream-auth-timeout");
            thread.setDaemon(true);
            return thread;
        });
        this.eventBus.observe(OpenttdTerminalUpdateEvent.class, this, this::broadcast);
    }

    @PreDestroy
    void destroy() {
        this.eventBus.unregister(this);
        if (this.authTimeoutExecutor != null) {
            this.authTimeoutExecutor.shutdownNow();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.pending.put(session.getId(), session);
        this.authTimeoutExecutor.schedule(() -> {
            if (this.pending.remove(session.getId()) != null) {
                LOG.debugf("Closing websocket '%s', it did not send a subscribe message", session.getId());
                close(session);
            }
        }, this.authTimeoutMs, TimeUnit.MILLISECONDS);
    }

    @OnClose
    public void onClose(Session session) {
        forget(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOG.debugf(throwable, "Websocket '%s' failed", session.getId());
        forget(session);
    }

    /**
     * Every frame of a client is a {@link SubscribeMessage}: the first one authenticates the connection, a later one
     * switches it to another process or asks for the output it missed.
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        SubscribeMessage subscribe = parse(message);
        if (subscribe == null || !subscribe.isSubscribe() || !this.securityService.isLoggedIn(subscribe.sessionId())) {
            LOG.debugf("Closing websocket '%s', its subscribe message was not accepted", session.getId());
            close(session);
            return;
        }
        this.securityService.refreshSession(subscribe.sessionId());
        this.pending.remove(session.getId());

        Subscription subscription = new Subscription(session, subscribe.processId());
        this.subscriptions.put(session.getId(), subscription);
        // From here on live batches are collected in the subscription, so that the client cannot see a batch before
        // the snapshot it deduplicates against.
        subscription.open(subscribe.processId() != null ? snapshot(subscribe.processId(), subscribe.fromOffset()) : null);
    }

    private SubscribeMessage parse(String message) {
        try {
            return MAPPER.readValue(message, SubscribeMessage.class);
        } catch (Exception e) {
            LOG.debugf(e, "Received a message that is not a subscribe message");
            return null;
        }
    }

    /**
     * @param processId  uuid of a process thread
     * @param fromOffset absolute offset the client already has
     * @return the snapshot of that process, an empty one if the process is unknown (it was stopped or the server was
     *         restarted, in which case the client has to pick up the new process id from 'GET /processes')
     */
    private String snapshot(String processId, Long fromOffset) {
        Optional<OpenttdProcess> process = this.openttdService.findProcessByThreadUuid(processId);
        if (process.isEmpty()) {
            return new OpenttdTerminalSnapshotEvent(this, processId, "", 0, null).toJson();
        }
        ProcessThread processThread = process.get().getProcessThread();
        ProcessThread.Snapshot snapshot = processThread.snapshot(fromOffset);
        return new OpenttdTerminalSnapshotEvent(this, processId, snapshot.text(), snapshot.endOffset(),
                processThread.getExitCode()).toJson();
    }

    private void broadcast(OpenttdTerminalUpdateEvent event) {
        String message = null;
        for (Subscription subscription : this.subscriptions.values()) {
            if (subscription.matches(event.getProcessId())) {
                if (message == null) {
                    message = event.toJson();
                }
                subscription.offer(message);
            }
        }
    }

    private void forget(Session session) {
        this.pending.remove(session.getId());
        this.subscriptions.remove(session.getId());
    }

    private void close(Session session) {
        forget(session);
        try {
            session.close(UNAUTHORIZED);
        } catch (IOException e) {
            LOG.debugf(e, "Failed to close websocket '%s'", session.getId());
        }
    }

    /**
     * One authenticated connection and the process it follows.
     */
    static final class Subscription {

        private final Session session;

        /**
         * The process this connection follows, null for "every process".
         */
        private final String processId;

        /**
         * Batches that arrived before the snapshot was written. Null once the snapshot is out and everything can be
         * sent directly. Guarded by the monitor of this instance.
         */
        private List<String> queued = new ArrayList<>();

        Subscription(Session session, String processId) {
            this.session = session;
            this.processId = processId;
        }

        boolean matches(String eventProcessId) {
            return this.processId == null || this.processId.equals(eventProcessId);
        }

        /**
         * Called on the pump thread of a process.
         */
        void offer(String message) {
            synchronized (this) {
                if (this.queued != null) {
                    this.queued.add(message);
                    return;
                }
            }
            send(message);
        }

        /**
         * Writes the snapshot, then everything that arrived while it was taken, and lets later batches through.
         *
         * @param snapshot the snapshot to write first, null for a subscription without one
         */
        void open(String snapshot) {
            synchronized (this) {
                if (snapshot != null) {
                    send(snapshot);
                }
                this.queued.forEach(this::send);
                this.queued = null;
            }
        }

        private void send(String message) {
            this.session.getAsyncRemote().sendText(message, result -> {
                if (result.getException() != null) {
                    LOG.debugf(result.getException(), "Unable to send a terminal event to websocket '%s'", this.session.getId());
                }
            });
        }
    }
}
