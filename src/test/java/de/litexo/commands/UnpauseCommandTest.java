package de.litexo.commands;

import de.litexo.ProcessThread;
import de.litexo.repository.DefaultRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnpauseCommandTest {

    @Mock
    ProcessThread process;

    @Mock()
    DefaultRepository repository;

    UnpauseCommand subject;

    @BeforeEach
    void beforeEach() {
        subject = new UnpauseCommand(repository);
        this.subject.marker = "@@@-xxx-asdasd";
        this.subject.pollIntervalMs = 1;
    }

    @Test
    void test001() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/unpause.txt", StandardCharsets.UTF_8);
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(logs);

        this.subject.execute(process, UUID.randomUUID().toString());

        assertTrue(this.subject.isExecuted());
    }

    @DisplayName("Test the console output of OpenTTD 15.3, which prefixes the message with U+200E")
    @Test
    void test002_openttd15Sample() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/unpause-15.3.txt", StandardCharsets.UTF_8);
        this.subject.marker = "@@@@_UnpauseCommand_1_@@@@";
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(logs);

        this.subject.execute(process, UUID.randomUUID().toString());

        assertTrue(this.subject.isExecuted());
        assertTrue(logs.contains("‎*** Game unpaused (manual)"), "The left-to-right mark of OpenTTD 15.3 was lost");
    }
}
