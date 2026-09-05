package de.litexo.commands;

import de.litexo.ProcessThread;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerInfoCommandTest {

    @Mock
    ProcessThread process;

    ServerInfoCommand subject = new ServerInfoCommand();

    @BeforeEach
    void beforeEach() {
        this.subject.marker = "@@@-xxx-asdasd";
        this.subject.pollIntervalMs = 1;
    }

    @Test
    void test001() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/serverInfo.txt", StandardCharsets.UTF_8);
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(logs);

        ServerInfoCommand execute = (ServerInfoCommand) this.subject.execute(process, UUID.randomUUID().toString());

        assertTrue(execute.isExecuted());
        assertEquals("asdasd",execute.getInviteCode());
        assertEquals(3,execute.getCurrentClients());
        assertEquals(25,execute.getMaxClients());
        assertEquals(5,execute.getCurrentCompanies());
        assertEquals(15,execute.getMaxCompanies());
        assertEquals(1,execute.getCurrentSpectators());
    }

    @DisplayName("Test the 'server_info' output of an idle OpenTTD 15.3 server")
    @Test
    void test002_openttd15Sample() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/server-info-15.3.txt", StandardCharsets.UTF_8);
        this.subject.marker = "@@@@_ServerInfoCommand_1_@@@@";
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(logs);

        ServerInfoCommand execute = (ServerInfoCommand) this.subject.execute(process, UUID.randomUUID().toString());

        assertTrue(execute.isExecuted());
        // An idle 15.3 server reports an empty invite code
        assertEquals("", execute.getInviteCode());
        assertEquals(0, execute.getCurrentClients());
        assertEquals(25, execute.getMaxClients());
        assertEquals(0, execute.getCurrentCompanies());
        assertEquals(15, execute.getMaxCompanies());
        assertEquals(0, execute.getCurrentSpectators());
    }

    @DisplayName("Test that 'echo <marker>' is not answered with a 'Command not found' line on OpenTTD 15.3")
    @Test
    void test003_echoMarkerIsSilent() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/echo-marker-15.3.txt", StandardCharsets.UTF_8);
        this.subject.marker = "@@@@_ServerInfoCommand_7_@@@@";
        when(this.process.getLogsLength()).thenReturn(0);
        when(this.process.getLogsFrom(0)).thenReturn(logs);

        ServerInfoCommand execute = (ServerInfoCommand) this.subject.execute(process, UUID.randomUUID().toString());

        assertTrue(execute.isExecuted());
        assertTrue(logs.contains("echo @@@@_ServerInfoCommand_7_@@@@\n@@@@_ServerInfoCommand_7_@@@@"),
                "OpenTTD 15.3 must echo the marker verbatim");
        assertTrue(!logs.contains("Command '@@@@"), "The marker must not produce a 'Command not found' line");
    }
}
