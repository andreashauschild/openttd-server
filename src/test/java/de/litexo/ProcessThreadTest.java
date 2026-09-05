package de.litexo;

import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@EnabledOnOs(OS.LINUX)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessThreadTest {

    private static final String EXIT_LINE_PREFIX = ProcessThread.EXIT_LINE_PREFIX;

    @Mock
    EventBus eventBus;

    ExecutorService executorService = Executors.newCachedThreadPool();

    @AfterEach
    void afterEach() {
        this.executorService.shutdownNow();
    }

    private ProcessThread start(String shellScript) {
        return start(shellScript, 10_000_000, 3_000_000);
    }

    private ProcessThread start(String shellScript, int maxLogChars, int keepLogChars) {
        ProcessThread processThread = new ProcessThread(this.executorService, List.of("sh", "-c", shellScript), this.eventBus, maxLogChars, keepLogChars);
        this.executorService.execute(processThread);
        return processThread;
    }

    private void awaitExitLine(ProcessThread processThread, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (processThread.getLogs().contains(EXIT_LINE_PREFIX)) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Process did not report its exit within " + timeoutMs + " ms. Logs: " + processThread.getLogs());
    }

    @DisplayName("Test that every stdout and stderr line reaches the logs exactly once")
    @Test
    void test_allStdoutAndStderrLinesAreCapturedExactlyOnce() throws Exception {
        ProcessThread processThread = start("for i in $(seq 1 2000); do echo out$i; echo err$i 1>&2; done");

        awaitExitLine(processThread, 30_000);
        // Leading newline so that the very first line is delimited like all others
        String logs = "\n" + processThread.getLogs();

        for (int i = 1; i <= 2000; i++) {
            assertEquals(1, countOccurrences(logs, "\nout" + i + "\n"), "stdout line out" + i);
            assertEquals(1, countOccurrences(logs, "\nerr" + i + "\n"), "stderr line err" + i);
        }
    }

    @DisplayName("Test that console input is echoed into the logs")
    @Test
    void test_inputIsEchoedIntoLogsBeforeChildOutput() throws Exception {
        ProcessThread processThread = start("cat");
        // Give the reader and writer threads a moment to attach to the streams
        Thread.sleep(300);

        processThread.write("hello");

        long deadline = System.currentTimeMillis() + 2_000;
        while (System.currentTimeMillis() < deadline && !processThread.getLogs().contains("hello\nhello\n")) {
            Thread.sleep(20);
        }
        assertTrue(processThread.getLogs().contains("hello\nhello\n"), "Logs were: " + processThread.getLogs());

        processThread.stop();
    }

    @DisplayName("Test that no reader or pump thread survives the process")
    @Test
    void test_pumpAndReaderThreadsTerminateAfterProcessExit() throws Exception {
        ProcessThread processThread = start("exit 0");

        awaitExitLine(processThread, 10_000);

        this.executorService.shutdown();
        assertTrue(this.executorService.awaitTermination(5, TimeUnit.SECONDS), "Threads of the process did not terminate");
    }

    @DisplayName("Test that the published terminal events carry exactly the text that was appended to the logs")
    @Test
    void test_publishesTerminalUpdateEventsWithBatchedText() throws Exception {
        ProcessThread processThread = start("for i in $(seq 1 200); do echo line$i; done");

        awaitExitLine(processThread, 15_000);
        // Let the pump publish the last batch
        Thread.sleep(300);

        ArgumentCaptor<OpenttdTerminalUpdateEvent> captor = ArgumentCaptor.forClass(OpenttdTerminalUpdateEvent.class);
        verify(this.eventBus, atLeastOnce()).publish(captor.capture());

        StringBuilder published = new StringBuilder();
        for (OpenttdTerminalUpdateEvent event : captor.getAllValues()) {
            assertEquals(processThread.getUuid(), event.getProcessId());
            published.append(event.getText());
        }
        assertEquals(processThread.getLogs(), published.toString());
    }

    @DisplayName("Test that the offsets of the published events are contiguous and that only the last one has the exit code")
    @Test
    void test_publishedEventsCarryContiguousOffsetsAndTheExitCode() throws Exception {
        ProcessThread processThread = start("for i in $(seq 1 200); do echo line$i; done");

        awaitExitLine(processThread, 15_000);
        Thread.sleep(300);

        List<OpenttdTerminalUpdateEvent> events = publishedEvents();
        long expectedOffset = 0;
        for (int i = 0; i < events.size(); i++) {
            OpenttdTerminalUpdateEvent event = events.get(i);
            assertEquals(expectedOffset, event.getOffset(), "Offset of batch " + i);
            expectedOffset += event.getText().length();
            if (i < events.size() - 1) {
                assertNull(event.getExitCode(), "Batch " + i + " must not carry an exit code");
            }
        }
        assertEquals(processThread.getAbsoluteEnd(), expectedOffset);
        assertEquals(Integer.valueOf(0), events.get(events.size() - 1).getExitCode());
    }

    @DisplayName("Test that the offsets stay absolute when the logs are trimmed")
    @Test
    void test_offsetsStayAbsoluteAcrossATrim() throws Exception {
        ProcessThread processThread = start("for i in $(seq 1 500); do echo 0123456789; done", 1000, 300);

        awaitExitLine(processThread, 15_000);
        Thread.sleep(300);

        List<OpenttdTerminalUpdateEvent> events = publishedEvents();
        long produced = 0;
        for (OpenttdTerminalUpdateEvent event : events) {
            assertEquals(produced, event.getOffset());
            produced += event.getText().length();
        }
        // The history is shorter than what the process produced, but the offsets still count every character
        assertTrue(processThread.getLogs().length() < produced, "The logs were not trimmed");
        assertEquals(produced, processThread.getAbsoluteEnd());
    }

    @DisplayName("Test that a snapshot returns exactly the text behind the given offset")
    @Test
    void test_snapshotFromOffsetReturnsOnlyTheMissingText() throws Exception {
        ProcessThread processThread = start("for i in $(seq 1 50); do echo line$i; done");

        awaitExitLine(processThread, 15_000);
        Thread.sleep(300);

        String logs = processThread.getLogs();
        long end = processThread.getAbsoluteEnd();
        assertEquals(logs.length(), end);

        ProcessThread.Snapshot snapshot = processThread.snapshot(10L);
        assertEquals(logs.substring(10), snapshot.text());
        assertEquals(end, snapshot.endOffset());
        assertEquals(Optional.of(logs.substring(10)), processThread.getLogsFromAbsolute(10));

        // Nothing is missing, the client is up to date
        assertEquals("", processThread.snapshot(end).text());
        assertEquals(Optional.of(""), processThread.getLogsFromAbsolute(end));

        // An offset behind the end of the history is not usable and falls back to the tail
        assertEquals(logs, processThread.snapshot(end + 1).text());
        assertEquals(Optional.empty(), processThread.getLogsFromAbsolute(end + 1));
    }

    @DisplayName("Test that a snapshot of an offset that was trimmed away falls back to the tail of the logs")
    @Test
    void test_snapshotOfTrimmedOffsetFallsBackToTail() throws Exception {
        ProcessThread processThread = start("for i in $(seq 1 500); do echo 0123456789; done", 1000, 300);

        awaitExitLine(processThread, 15_000);
        Thread.sleep(300);

        ProcessThread.Snapshot snapshot = processThread.snapshot(0L);
        assertEquals(processThread.getLogs(), snapshot.text());
        assertEquals(processThread.getAbsoluteEnd(), snapshot.endOffset());
        assertEquals(Optional.empty(), processThread.getLogsFromAbsolute(0));
    }

    @DisplayName("Test that the exit code is reported on the model of the process")
    @Test
    void test_exitCodeIsReportedOnTheModel() throws Exception {
        ProcessThread processThread = start("exit 3");

        awaitExitLine(processThread, 10_000);
        Thread.sleep(300);

        assertEquals(Integer.valueOf(3), processThread.getExitCode());
        assertEquals(Integer.valueOf(3), processThread.toModel().getExitCode());
        assertEquals(processThread.getLogs(), processThread.toModel().getProcessData());
    }

    private List<OpenttdTerminalUpdateEvent> publishedEvents() {
        ArgumentCaptor<OpenttdTerminalUpdateEvent> captor = ArgumentCaptor.forClass(OpenttdTerminalUpdateEvent.class);
        verify(this.eventBus, atLeastOnce()).publish(captor.capture());
        return captor.getAllValues();
    }

    @DisplayName("Test that the logs are trimmed when the limit is exceeded")
    @Test
    void test_logsAreTrimmedAboveLimit() throws Exception {
        ProcessThread processThread = start("for i in $(seq 1 500); do echo 0123456789; done", 1000, 300);

        awaitExitLine(processThread, 15_000);
        Thread.sleep(300);

        String logs = processThread.getLogs();
        assertTrue(logs.length() <= 1000, "Logs were not trimmed, length was " + logs.length());
        assertTrue(logs.endsWith(EXIT_LINE_PREFIX + "0\n"), "Tail of the logs was lost: " + logs);
        assertTrue(logs.contains("0123456789"), "Tail of the logs was lost: " + logs);
    }

    @DisplayName("Test that a process that reacts on 'quit' is stopped without being killed")
    @Test
    void test_stopGracefullyReturnsTrueWhenChildExitsOnQuit() throws Exception {
        ProcessThread processThread = start("read l; echo bye; exit 0");
        Thread.sleep(300);

        assertTrue(processThread.stopGracefully(5000));
        assertFalse(processThread.isAlive());

        awaitExitLine(processThread, 5_000);
        String logs = processThread.getLogs();
        assertTrue(logs.contains("bye"), "Logs were: " + logs);
        assertTrue(logs.contains(EXIT_LINE_PREFIX + "0"), "Logs were: " + logs);
    }

    @DisplayName("Test that a process that ignores 'quit' is killed after the timeout")
    @Test
    void test_stopGracefullyKillsAfterTimeout() throws Exception {
        ProcessThread processThread = start("trap '' TERM; sleep 60");
        Thread.sleep(300);

        assertFalse(processThread.stopGracefully(300));

        long deadline = System.currentTimeMillis() + 6_000;
        while (System.currentTimeMillis() < deadline && processThread.isAlive()) {
            Thread.sleep(20);
        }
        assertFalse(processThread.isAlive(), "Process was not killed");
    }

    private int countOccurrences(String text, String needle) {
        int count = 0;
        int index = text.indexOf(needle);
        while (index > -1) {
            count++;
            index = text.indexOf(needle, index + 1);
        }
        return count;
    }

    @DisplayName("Test that two console writes are spaced apart, the OpenTTD console reads only one line per read")
    @Test
    void test_consoleWritesAreSpacedApart() throws Exception {
        ProcessThread processThread = start("cat");
        Thread.sleep(300);

        long start = System.currentTimeMillis();
        processThread.write("first");
        processThread.write("second");

        long deadline = start + 5_000;
        while (System.currentTimeMillis() < deadline
                && !(processThread.getLogs().contains("first") && processThread.getLogs().contains("second"))) {
            Thread.sleep(10);
        }
        long elapsed = System.currentTimeMillis() - start;

        String logs = processThread.getLogs();
        assertTrue(logs.contains("first"), "Logs were: " + logs);
        assertTrue(logs.contains("second"), "Logs were: " + logs);
        assertTrue(elapsed >= ProccesInputThread.MIN_WRITE_GAP_MS,
                "The two commands were written without a gap, elapsed " + elapsed + " ms");

        processThread.stop();
    }
}
