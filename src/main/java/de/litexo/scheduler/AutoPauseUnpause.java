package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.ServerInfoCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;
import de.litexo.services.OpenttdService;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@ApplicationScoped
public class AutoPauseUnpause {

    private static final Logger LOG = Logger.getLogger(AutoPauseUnpause.class);

    @Inject
    OpenttdService service;

    @Inject
    DefaultRepository repository;

    @Inject
    EventBus eventBus;

    @Inject
    ManagedExecutor executor;

    @Inject
    UpdateServerInfo updateServerInfo;

    /**
     * One lock per process. Reading the client count and acting on it is one decision, so the whole sequence
     * ('server_info', decision, 'pause'/'unpause') has to be atomic per server. The console lock of
     * {@link OpenttdProcess} only serializes the single commands, which still allowed the scheduled tick to decide on
     * an outdated client count and send its 'pause' after the event driven check had already sent an 'unpause'.
     */
    private final Map<String, ReentrantLock> checkLocks = new ConcurrentHashMap<>();

    /**
     * One flag per process, so that a burst of join and leave lines does not start a check per line. An event that
     * arrives while a check is running is not dropped, it is remembered here and answered by exactly one additional
     * check after the running one, which always sees the newest state of the whole burst.
     */
    private final Map<String, AtomicBoolean> rerunRequests = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        LOG.info("INIT AutoPauseUnpause Scheduler");
        this.eventBus.observe(OpenttdTerminalUpdateEvent.class, this.getClass(), openttdTerminalUpdateEvent -> {
            // Important execute this async because the subscriber is blocking the emitter of the event
            executor.execute(() -> handleTerminalUpdateEvent(openttdTerminalUpdateEvent));
        });
    }

    @Scheduled(every = "30s", concurrentExecution = ConcurrentExecution.SKIP)
    void checkAutoPauseUnpause() {
        for (OpenttdProcess process : service.getProcesses()) {
            doPauseOrUnpause(process);
        }
    }

    /**
     * Periodic entry point. Waits for a running check of the same server instead of deciding in parallel with it.
     */
    void doPauseOrUnpause(OpenttdProcess process) {
        if (!isCheckable(process)) {
            return;
        }
        ReentrantLock lock = checkLock(process.getId());
        lock.lock();
        try {
            checkAndRerunWhileRequested(process);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Event driven entry point. A check that is already running for this server sees the new state anyway, so this
     * only asks for one additional check afterwards instead of queueing behind the running one.
     */
    void doPauseOrUnpauseCoalesced(OpenttdProcess process) {
        if (!isCheckable(process)) {
            return;
        }
        ReentrantLock lock = checkLock(process.getId());
        if (!lock.tryLock()) {
            rerunRequest(process.getId()).set(true);
            LOG.debugf("A pause/unpause check for process '%s' is already running, a re-run was requested", process.getId());
            return;
        }
        try {
            checkAndRerunWhileRequested(process);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Must only be called while the lock of this process is held.
     */
    private void checkAndRerunWhileRequested(OpenttdProcess process) {
        AtomicBoolean rerunRequested = rerunRequest(process.getId());
        check(process);
        while (rerunRequested.compareAndSet(true, false)) {
            LOG.debugf("Re-running the pause/unpause check of process '%s' for events that arrived while it was running", process.getId());
            check(process);
        }
    }

    private boolean isCheckable(OpenttdProcess process) {
        if (!process.isAlive()) {
            LOG.debugf("Pause/unpause check of server '%s' is skipped, its process is not running", process.getId());
            return false;
        }
        return true;
    }

    private ReentrantLock checkLock(String processId) {
        return this.checkLocks.computeIfAbsent(processId, key -> new ReentrantLock(true));
    }

    private AtomicBoolean rerunRequest(String processId) {
        return this.rerunRequests.computeIfAbsent(processId, key -> new AtomicBoolean(false));
    }

    /**
     * Reads the current client count and pauses the game while nobody is connected. A connected client keeps the game
     * running, no matter whether it already joined a company, so a spectator is a player as well.
     */
    private void check(OpenttdProcess process) {
        OpenttdServer openttdServer = service.getOpenttdServer(process.getId()).orElse(null);

        if (openttdServer != null) {
            if (!openttdServer.isAutoPause()) {
                LOG.debugf("Autopause is disabled for Server: %s", openttdServer.getName());
                return;
            }

            ServerInfoCommand cmd = process.executeCommand(new ServerInfoCommand(), false);
            if (cmd.isExecuted()) {
                if (cmd.getCurrentClients() == 0) {
                    LOG.debugf("Pause Server: %s", process.getId());
                    PauseCommand pauseCommand = process.executeCommand(new PauseCommand(this.repository), false);
                    if (pauseCommand.isExecuted()) {
                        openttdServer.setPaused(true);
                    }
                } else {
                    LOG.debugf("Unpause Server: %s", process.getId());
                    UnpauseCommand unpauseCommand = process.executeCommand(new UnpauseCommand(this.repository), false);
                    if (unpauseCommand.isExecuted()) {
                        openttdServer.setPaused(false);
                    }
                }
                // Reuse the result instead of sending a second 'server_info' from UpdateServerInfo.
                this.updateServerInfo.applyServerInfo(openttdServer, cmd);
            }
        }
    }

    void handleTerminalUpdateEvent(OpenttdTerminalUpdateEvent openttdTerminalUpdateEvent) {
        if (
                openttdTerminalUpdateEvent.getText().contains("has started a new company")
                        || openttdTerminalUpdateEvent.getText().contains("has joined company")
                        || openttdTerminalUpdateEvent.getText().contains("has joined the game")
                        || openttdTerminalUpdateEvent.getText().contains("has left the game")
                        || openttdTerminalUpdateEvent.getText().contains("closed connection")
                        || openttdTerminalUpdateEvent.getText().contains("has joined spectators")
        ) {
            Optional<OpenttdProcess> process = this.service.findProcessByThreadUuid(openttdTerminalUpdateEvent.getProcessId());
            if (process.isPresent()) {
                doPauseOrUnpauseCoalesced(process.get());
            }
        }
    }
}
