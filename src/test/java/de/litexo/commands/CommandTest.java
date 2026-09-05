package de.litexo.commands;

import de.litexo.ProcessThread;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommandTest {

    private static final String SUCCESS_LINE = "*** Game paused (manual)";
    private static final String FAILURE_LINE = "Saving map failed.";

    @Mock
    ProcessThread process;

    /**
     * Minimal concrete command that records what the base class did.
     */
    private static class TestCommand extends Command {

        int onSuccessCalls = 0;
        String onSuccessServerId;

        TestCommand() {
            super("pause");
            this.pollIntervalMs = 1;
            this.timeoutMs = 2_000;
        }

        @Override
        public boolean check(String logs) {
            if (logs.contains(FAILURE_LINE)) {
                fail(FAILURE_LINE);
                return false;
            }
            return logs.contains(SUCCESS_LINE);
        }

        @Override
        public void onSuccess(String openttdServeId) {
            this.onSuccessCalls++;
            this.onSuccessServerId = openttdServeId;
        }
    }

    @DisplayName("Test that the echo marker is written before the command itself")
    @Test
    void test_executeWritesEchoMarkerThenCommand() {
        TestCommand subject = new TestCommand();
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(subject.marker + "\npause\n" + SUCCESS_LINE + "\n");

        subject.execute(this.process, "server-1");

        InOrder inOrder = inOrder(this.process);
        inOrder.verify(this.process).write("echo " + subject.marker);
        inOrder.verify(this.process).write("pause");
        assertTrue(subject.isExecuted());
        assertEquals(1, subject.onSuccessCalls);
        assertEquals("server-1", subject.onSuccessServerId);
    }

    @DisplayName("Test that only the output written after the start index is scanned")
    @Test
    void test_executeOnlyScansOutputAfterStartIndex() {
        TestCommand subject = new TestCommand();
        when(this.process.getLogsLength()).thenReturn(500);
        // A stale success line from an earlier command must not be seen
        when(this.process.getLogsFrom(0)).thenReturn(subject.marker + "\n" + SUCCESS_LINE + "\nstale\n");
        when(this.process.getLogsFrom(500)).thenReturn(subject.marker + "\npause\n" + SUCCESS_LINE + "\n");

        subject.execute(this.process, UUID.randomUUID().toString());

        assertTrue(subject.isExecuted());
        verify(this.process, never()).getLogsFrom(0);
        verify(this.process).getLogsFrom(500);
    }

    @DisplayName("Test that the command gives up after the timeout")
    @Test
    void test_executeTimesOut() {
        TestCommand subject = new TestCommand();
        subject.pollIntervalMs = 10;
        subject.timeoutMs = 100;
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(subject.marker + "\npause\n");
        when(this.process.isAlive()).thenReturn(true);

        subject.execute(this.process, UUID.randomUUID().toString());

        assertFalse(subject.isExecuted());
        assertFalse(subject.isFailed());
        assertEquals(0, subject.onSuccessCalls);
    }

    @DisplayName("Test that a command gives up right away when the process died before it could answer")
    @Test
    void test_failsFastWhenProcessDied() {
        TestCommand subject = new TestCommand();
        subject.pollIntervalMs = 10;
        subject.timeoutMs = 60_000;
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(subject.marker + "\npause\n");
        when(this.process.isAlive()).thenReturn(false);

        long start = System.currentTimeMillis();
        subject.execute(this.process, UUID.randomUUID().toString());
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(subject.isFailed());
        assertEquals(Command.PROCESS_EXITED, subject.getFailureReason());
        assertFalse(subject.isExecuted());
        assertEquals(0, subject.onSuccessCalls);
        assertTrue(elapsed < 5_000, "The command waited for the full timeout, elapsed " + elapsed + " ms");
    }

    @DisplayName("Test that output the process wrote before it exited still counts as a result")
    @Test
    void test_resultWrittenBeforeTheExitIsStillAccepted() {
        TestCommand subject = new TestCommand();
        subject.timeoutMs = 60_000;
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(subject.marker + "\npause\n" + SUCCESS_LINE + "\n");
        when(this.process.isAlive()).thenReturn(false);

        subject.execute(this.process, "server-1");

        assertTrue(subject.isExecuted());
        assertFalse(subject.isFailed());
        assertEquals(1, subject.onSuccessCalls);
    }

    @DisplayName("Test that a negative result stops the wait immediately")
    @Test
    void test_failStopsWaitingEarly() {
        TestCommand subject = new TestCommand();
        subject.timeoutMs = 60_000;
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(subject.marker + "\npause\n" + FAILURE_LINE + "\n");

        long start = System.currentTimeMillis();
        subject.execute(this.process, UUID.randomUUID().toString());
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(subject.isFailed());
        assertEquals(FAILURE_LINE, subject.getFailureReason());
        assertFalse(subject.isExecuted());
        assertEquals(0, subject.onSuccessCalls);
        assertTrue(elapsed < 5_000, "The command waited for the full timeout, elapsed " + elapsed + " ms");
    }
}
