package de.litexo;

import de.litexo.commands.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenttdProcessTest {

    @Mock
    ProcessThread processThread;

    /**
     * Records when it entered and left {@link #execute} so that overlapping executions can be detected.
     */
    private static class RecordingCommand extends Command {

        private final List<long[]> intervals;

        private boolean ran = false;

        RecordingCommand(List<long[]> intervals) {
            super("server_info");
            this.intervals = intervals;
        }

        @Override
        public boolean check(String logs) {
            return true;
        }

        @Override
        public Command execute(ProcessThread process, String openttdServerId) {
            long enter = System.nanoTime();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.intervals.add(new long[]{enter, System.nanoTime()});
            this.ran = true;
            return this;
        }
    }

    @DisplayName("Test that two commands for the same process never run at the same time")
    @Test
    void test_executeCommandIsSerializedPerProcess() throws Exception {
        OpenttdProcess subject = new OpenttdProcess().setId("id1").setProcessThread(this.processThread);

        List<long[]> intervals = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            executorService.execute(() -> subject.executeCommand(new RecordingCommand(intervals), true));
            executorService.execute(() -> subject.executeCommand(new RecordingCommand(intervals), true));
            executorService.shutdown();
            assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
        } finally {
            executorService.shutdownNow();
        }

        assertEquals(2, intervals.size());
        long[] first = intervals.get(0);
        long[] second = intervals.get(1);
        assertTrue(first[1] <= second[0], "The two commands overlapped");
    }

    @DisplayName("Test that automatic commands are skipped while the ui terminal is open")
    @Test
    void test_executeCommandSkippedWhenUiTerminalOpen() {
        OpenttdProcess subject = new OpenttdProcess().setId("id1").setProcessThread(this.processThread);
        subject.setLastUiTerminalActivity(System.currentTimeMillis());

        List<long[]> intervals = Collections.synchronizedList(new ArrayList<>());

        RecordingCommand skipped = subject.executeCommand(new RecordingCommand(intervals), false);
        assertFalse(skipped.ran);
        assertTrue(intervals.isEmpty());
        // The skipped commands are not written into the console history, the counter is their only trace
        assertEquals(1, subject.getSkippedCommands().get());

        // An explicitly requested command runs even with an open ui terminal
        RecordingCommand forced = subject.executeCommand(new RecordingCommand(intervals), true);
        assertTrue(forced.ran);
        assertEquals(1, intervals.size());
        assertEquals(1, subject.getSkippedCommands().get());
    }
}
