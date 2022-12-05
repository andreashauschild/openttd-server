package de.litexo.commands;

import de.litexo.ProcessThread;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientsCommandTest {

    @Mock
    ProcessThread process;

    ClientsCommand subject = new ClientsCommand();

    @BeforeEach
    void beforeEach() {
        this.subject.marker = "@@@-xxx-asdasd";
    }

    @Test
    void test001() throws Exception {
        String logs = IOUtils.resourceToString("/command-samples/clients.txt", StandardCharsets.UTF_8);
        when(this.process.getLogs()).thenReturn(logs);

        ClientsCommand execute = (ClientsCommand) this.subject.execute(process);

        assertTrue(execute.isExecuted());
        assertEquals(3,execute.getClients().size());

        assertEquals("1",execute.getClients().get(0).getIndex());
        assertEquals("Andreas Hauschild'",execute.getClients().get(0).getName());
        assertEquals("255",execute.getClients().get(0).getCompany());
        assertEquals("server",execute.getClients().get(0).getIp());

        assertEquals("2",execute.getClients().get(1).getIndex());
        assertEquals("Unnamed Client",execute.getClients().get(1).getName());
        assertEquals("255",execute.getClients().get(1).getCompany());
        assertEquals("10.0.2.15",execute.getClients().get(1).getIp());

        assertEquals("4",execute.getClients().get(2).getIndex());
        assertEquals("Unnamed Client #1",execute.getClients().get(2).getName());
        assertEquals("255",execute.getClients().get(2).getCompany());
        assertEquals("10.0.2.15",execute.getClients().get(2).getIp());

        System.out.println(execute.getClients());
    }

}
