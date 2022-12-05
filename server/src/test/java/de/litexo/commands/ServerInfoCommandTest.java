package de.litexo.commands;

import de.litexo.ProcessThread;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

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
    }

    @Test
    void test001() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/serverInfo.txt", StandardCharsets.UTF_8);
        when(this.process.getLogs()).thenReturn(logs);

        ServerInfoCommand execute = (ServerInfoCommand) this.subject.execute(process);

        assertTrue(execute.isExecuted());
        assertEquals("asdasd",execute.getInviteCode());
        assertEquals(3,execute.getCurrentClients());
        assertEquals(25,execute.getMaxClients());
        assertEquals(5,execute.getCurrentCompanies());
        assertEquals(15,execute.getMaxCompanies());
        assertEquals(1,execute.getCurrentSpectators());
    }

}
