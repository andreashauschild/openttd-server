package de.litexo.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.litexo.OpenttdProcess;
import de.litexo.ProcessThread;
import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import de.litexo.security.BasicAuthSession;
import de.litexo.security.SecurityService;
import de.litexo.security.SecurityUtils;
import de.litexo.services.OpenttdService;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the wire protocol of '/data-stream' with the websocket client of the jdk, because the application itself only
 * needs the server side of 'quarkus-websockets'.
 * <p>
 * The 'start-server.command' of the test profile is 'sh -c cat', so a "server" started here behaves like the OpenTTD
 * console in the only aspect that matters for the socket: everything written to it comes back as output.
 */
@EnabledOnOs(OS.LINUX)
@QuarkusTest
class DataStreamWebSocketTest {

    /**
     * admin:Password_1
     */
    private static final String BASIC_AUTH_HEADER = "Basic YWRtaW46UGFzc3dvcmRfMQ==";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * A process id no process ever had, i.e. a terminal that was open while the server was stopped.
     */
    private static final String UNKNOWN_PROCESS_ID = "a-process-that-does-not-exist";

    private static final int VIOLATED_POLICY = 1008;

    @TestHTTPResource("/data-stream")
    URI httpUri;

    @Inject
    SecurityService securityService;

    @Inject
    DefaultRepository repository;

    @Inject
    OpenttdService openttdService;

    @Inject
    EventBus eventBus;

    private final List<WebSocket> openSockets = new ArrayList<>();

    private final List<String> startedServers = new ArrayList<>();

    private String sessionId;

    @BeforeEach
    void beforeEach() {
        // The password hash is written on the first start only, so a repeated run without 'clean' would still use the
        // hash of the previous run.
        InternalOpenttdServerConfig config = this.repository.getOpenttdServerConfig();
        config.setPasswordSha256Hash(SecurityUtils.toSHA256("Password_1"));
        this.repository.save(config);

        Optional<BasicAuthSession> session = this.securityService.login(BASIC_AUTH_HEADER);
        assertTrue(session.isPresent(), "Could not log in");
        this.sessionId = session.get().getSessionId();
    }

    @AfterEach
    void afterEach() {
        this.openSockets.forEach(WebSocket::abort);
        for (String serverId : this.startedServers) {
            // Kill the process first, 'cat' does not react on 'quit' and the graceful stop would wait for its timeout
            processOf(serverId).map(OpenttdProcess::getProcessThread).ifPresent(ProcessThread::stop);
            this.openttdService.deleteServer(serverId);
        }
        this.securityService.logout(this.sessionId);
    }

    @DisplayName("Test that a connection whose first frame carries an unknown session id is closed with 1008")
    @Test
    void test_unauthorizedSubscribeIsClosed() throws Exception {
        TestClient client = connect();

        client.send(subscribe("not-a-session", null, null));

        assertEquals(Integer.valueOf(VIOLATED_POLICY), client.closeCode.get(5, TimeUnit.SECONDS));
    }

    @DisplayName("Test that a connection that does not subscribe at all is closed with 1008")
    @Test
    void test_silentConnectionIsClosedAfterTheAuthTimeout() throws Exception {
        TestClient client = connect();

        // 'server.websocket.auth-timeout-ms' is 500 ms in the test profile
        assertEquals(Integer.valueOf(VIOLATED_POLICY), client.closeCode.get(5, TimeUnit.SECONDS));
    }

    @DisplayName("Test that a connection whose first frame is not a subscribe message is closed with 1008")
    @Test
    void test_unknownMessageTypeIsClosed() throws Exception {
        TestClient client = connect();

        client.send("{\"type\":\"something-else\",\"sessionId\":\"" + this.sessionId + "\"}");

        assertEquals(Integer.valueOf(VIOLATED_POLICY), client.closeCode.get(5, TimeUnit.SECONDS));
    }

    @DisplayName("Test that a subscription without a process id receives the updates of every process and no snapshot")
    @Test
    void test_dashboardSubscriptionReceivesEveryUpdate() throws Exception {
        TestClient client = connect();
        client.send(subscribe(this.sessionId, null, null));

        // A subscription without a process id is not answered, so there is nothing to wait for before publishing.
        // Publishing until the first event arrives is the deterministic way to bridge the registration.
        JsonNode event = null;
        long deadline = System.currentTimeMillis() + 10_000;
        while (event == null && System.currentTimeMillis() < deadline) {
            this.eventBus.publish(new OpenttdTerminalUpdateEvent(this, UNKNOWN_PROCESS_ID, "hello\n", 17, 2));
            event = client.pollEvent(200);
        }

        assertNotNull(event, "The dashboard subscription did not receive the published event");
        assertEquals("OpenttdTerminalUpdateEvent", event.get("_type").asText());
        assertEquals(UNKNOWN_PROCESS_ID, event.get("processId").asText());
        assertEquals("hello\n", event.get("text").asText());
        assertEquals(17, event.get("offset").asLong());
        assertEquals(2, event.get("exitCode").asInt());
        assertTrue(event.get("created").asLong() > 0);
        assertNotNull(event.get("source"));
    }

    @DisplayName("Test that a subscription for a process that is gone is answered with an empty snapshot")
    @Test
    void test_snapshotOfAnUnknownProcessIsEmpty() throws Exception {
        TestClient client = connect();

        client.send(subscribe(this.sessionId, UNKNOWN_PROCESS_ID, null));

        JsonNode snapshot = client.nextEvent();
        assertEquals("OpenttdTerminalSnapshotEvent", snapshot.get("_type").asText());
        assertEquals(UNKNOWN_PROCESS_ID, snapshot.get("processId").asText());
        assertEquals("", snapshot.get("text").asText());
        assertEquals(0, snapshot.get("endOffset").asLong());
        assertTrue(snapshot.get("exitCode").isNull());
    }

    @DisplayName("Test that a process subscription is answered with the snapshot first and then with contiguous updates")
    @Test
    void test_snapshotFirstThenContiguousUpdates() throws Exception {
        String serverId = startServer();
        ProcessThread processThread = processThreadOf(serverId);
        write(serverId, "before-the-snapshot");

        TestClient client = connect();
        client.send(subscribe(this.sessionId, processThread.getUuid(), null));

        JsonNode snapshot = client.nextEvent();
        assertEquals("OpenttdTerminalSnapshotEvent", snapshot.get("_type").asText());
        assertEquals(processThread.getUuid(), snapshot.get("processId").asText());
        assertTrue(snapshot.get("text").asText().contains("before-the-snapshot"), "Snapshot was: " + snapshot);
        assertTrue(snapshot.get("exitCode").isNull());
        long endOffset = snapshot.get("endOffset").asLong();

        write(serverId, "after-the-snapshot");

        // What a client keeps has to line up behind the snapshot without a gap and without a duplicate. 'cat' echoes,
        // so the line arrives twice: once as the echo of the input and once as the output of the process.
        String expectedTail = "after-the-snapshot\nafter-the-snapshot\n";
        StringBuilder replayed = new StringBuilder(snapshot.get("text").asText());
        long expectedOffset = endOffset;
        while (replayed.indexOf(expectedTail) < 0) {
            JsonNode update = client.nextEvent();
            assertEquals("OpenttdTerminalUpdateEvent", update.get("_type").asText());
            assertEquals(processThread.getUuid(), update.get("processId").asText());
            if (update.get("offset").asLong() < endOffset) {
                // The dedupe rule of the protocol: this batch is part of the snapshot already
                continue;
            }
            assertEquals(expectedOffset, update.get("offset").asLong(), "The updates behind the snapshot are not contiguous");
            replayed.append(update.get("text").asText());
            expectedOffset += update.get("text").asText().length();
        }
        assertEquals(processThread.getLogs(), replayed.toString());
    }

    @DisplayName("Test that a subscription with 'fromOffset' is answered with exactly the text the client is missing")
    @Test
    void test_catchUpFromOffsetReturnsOnlyTheMissingText() throws Exception {
        String serverId = startServer();
        ProcessThread processThread = processThreadOf(serverId);
        write(serverId, "first-line");
        long fromOffset = processThread.getAbsoluteEnd();
        write(serverId, "second-line");

        TestClient client = connect();
        client.send(subscribe(this.sessionId, processThread.getUuid(), fromOffset));

        JsonNode snapshot = client.nextEvent();
        String missing = snapshot.get("text").asText();
        assertEquals(processThread.getLogs().substring((int) fromOffset), missing);
        assertTrue(missing.startsWith("second-line"), "Snapshot was: " + missing);
        assertEquals(processThread.getAbsoluteEnd(), snapshot.get("endOffset").asLong());
    }

    @DisplayName("Test that a second subscribe message switches the connection to another process")
    @Test
    void test_reSubscribeSwitchesTheProcess() throws Exception {
        String serverId = startServer();
        ProcessThread processThread = processThreadOf(serverId);
        TestClient client = connect();

        client.send(subscribe(this.sessionId, UNKNOWN_PROCESS_ID, null));
        assertEquals(UNKNOWN_PROCESS_ID, client.nextEvent().get("processId").asText());

        client.send(subscribe(this.sessionId, processThread.getUuid(), null));
        JsonNode snapshot = client.nextEvent();
        assertEquals("OpenttdTerminalSnapshotEvent", snapshot.get("_type").asText());
        assertEquals(processThread.getUuid(), snapshot.get("processId").asText());
    }

    private String subscribe(String sessionId, String processId, Long fromOffset) throws Exception {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "subscribe");
        message.put("sessionId", sessionId);
        message.put("processId", processId);
        message.put("fromOffset", fromOffset);
        return MAPPER.writeValueAsString(message);
    }

    private TestClient connect() throws Exception {
        TestClient client = new TestClient();
        URI wsUri = URI.create("ws://" + this.httpUri.getAuthority() + this.httpUri.getPath());
        WebSocket socket = HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(wsUri, client)
                .get(10, TimeUnit.SECONDS);
        this.openSockets.add(socket);
        client.socket = socket;
        return client;
    }

    private String startServer() {
        OpenttdServer server = this.openttdService.addServer(new OpenttdServer().setName("data-stream-test"));
        this.startedServers.add(server.getId());
        this.openttdService.startServer(server.getId());

        ProcessThread processThread = processThreadOf(server.getId());
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline && !processThread.isAlive()) {
            sleep();
        }
        assertTrue(processThread.isAlive(), "The test process did not start");
        return server.getId();
    }

    /**
     * Writes a line to the console and waits until it is visible twice: once as the echo of the input and once as the
     * output of 'cat'.
     */
    private void write(String serverId, String line) {
        ProcessThread processThread = processThreadOf(serverId);
        String echoed = line + "\n" + line + "\n";
        this.openttdService.sendTerminalCommand(serverId, line);
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline && !processThread.getLogs().contains(echoed)) {
            sleep();
        }
        assertTrue(processThread.getLogs().contains(echoed), "Logs were: " + processThread.getLogs());
    }

    private Optional<OpenttdProcess> processOf(String serverId) {
        return this.openttdService.getProcesses().stream()
                .filter(process -> serverId.equals(process.getId()))
                .findAny();
    }

    private ProcessThread processThreadOf(String serverId) {
        return processOf(serverId)
                .map(OpenttdProcess::getProcessThread)
                .orElseThrow(() -> new AssertionError("Server '" + serverId + "' is not running"));
    }

    private void sleep() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class TestClient implements WebSocket.Listener {

        private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        private final CompletableFuture<Integer> closeCode = new CompletableFuture<>();

        private final StringBuilder partialMessage = new StringBuilder();

        private WebSocket socket;

        void send(String message) throws Exception {
            this.socket.sendText(message, true).get(5, TimeUnit.SECONDS);
        }

        JsonNode nextEvent() throws Exception {
            JsonNode event = pollEvent(10_000);
            assertNotNull(event, "No event arrived. Close code: " + (this.closeCode.isDone() ? this.closeCode.get() : "still open"));
            return event;
        }

        JsonNode pollEvent(long timeoutMs) throws Exception {
            String message = this.messages.poll(timeoutMs, TimeUnit.MILLISECONDS);
            return message != null ? MAPPER.readTree(message) : null;
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            this.partialMessage.append(data);
            if (last) {
                this.messages.add(this.partialMessage.toString());
                this.partialMessage.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            this.closeCode.complete(statusCode);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            this.closeCode.completeExceptionally(error);
        }
    }
}
