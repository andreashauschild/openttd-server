package de.litexo.commands;

import de.litexo.ProcessThread;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaveCommandTest {

    private static final String SAVED_PATH = "/home/openttd/server/save/id1_manually_save_1700000000000";

    @Mock
    ProcessThread process;

    private SaveCommand subject(String path, String sampleResource) throws Exception {
        SaveCommand subject = new SaveCommand(path);
        subject.marker = "@@@@_SaveCommand_1_@@@@";
        subject.pollIntervalMs = 1;
        subject.timeoutMs = 500;
        String logs = IOUtils.resourceToString(sampleResource, StandardCharsets.UTF_8);
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(logs);
        when(this.process.isAlive()).thenReturn(true);
        return subject;
    }

    @DisplayName("Test that the confirmation of OpenTTD 15.3 for the requested path is accepted")
    @Test
    void test_successForMatchingPath() throws Exception {
        SaveCommand subject = subject(SAVED_PATH, "/command-samples/save-success-15.3.txt");

        subject.execute(this.process, UUID.randomUUID().toString());

        assertTrue(subject.isExecuted());
        assertFalse(subject.isFailed());
        assertEquals(SAVED_PATH + ".sav", subject.getSavedPath());
        assertTrue(subject.getSavedPath().endsWith(".sav"));
    }

    @DisplayName("Test that a confirmation for another save game is ignored")
    @Test
    void test_successForOtherPathIsIgnored() throws Exception {
        SaveCommand subject = subject("/home/openttd/server/save/id1_auto_save_1700000000001", "/command-samples/save-success-15.3.txt");

        subject.execute(this.process, UUID.randomUUID().toString());

        assertFalse(subject.isExecuted());
        assertFalse(subject.isFailed());
        assertNull(subject.getSavedPath());
    }

    @DisplayName("Test that 'Saving map failed.' marks the command as failed")
    @Test
    void test_failureSetsFailed() throws Exception {
        SaveCommand subject = subject(SAVED_PATH, "/command-samples/save-failed-15.3.txt");

        subject.execute(this.process, UUID.randomUUID().toString());

        assertTrue(subject.isFailed());
        assertFalse(subject.isExecuted());
        assertEquals("Saving map failed", subject.getFailureReason());
        assertNull(subject.getSavedPath());
    }

    @DisplayName("Test that the path of the console command is quoted")
    @Test
    void test_commandStringQuotesPath() {
        SaveCommand subject = new SaveCommand("/home/openttd/server/save/my save");

        assertEquals("save \"/home/openttd/server/save/my save\"", subject.command);
    }
}
