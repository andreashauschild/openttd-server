package de.litexo.scheduler;

import de.litexo.OpenttdProcess;
import de.litexo.ProcessThread;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.ServerInfoCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.services.OpenttdService;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoPauseUnpauseTest {


    @Mock
    OpenttdService service;

    @Mock
    OpenttdProcess openttdProcess;

    @Mock
    ProcessThread processThread;

    @Mock
    ServerInfoCommand serverInfoCommand;

    @InjectMocks
    AutoPauseUnpause subject = new AutoPauseUnpause();

    @DisplayName("Test pause because no client connected")
    @Test
    void test001() throws Exception {
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class))).thenReturn(serverInfoCommand);
        when(serverInfoCommand.getCurrentCompanies()).thenReturn(0);
        when(serverInfoCommand.isExecuted()).thenReturn(true);
        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess).executeCommand(any(PauseCommand.class));

    }

    @DisplayName("Test unpause because no client connected to company")
    @Test
    void test010() throws Exception {
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class))).thenReturn(serverInfoCommand);
        when(serverInfoCommand.getCurrentCompanies()).thenReturn(5);
        when(serverInfoCommand.isExecuted()).thenReturn(true);
        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess).executeCommand(any(UnpauseCommand.class));

    }

    @DisplayName("Test handleTerminalUpdateEvent")
    @Test
    void test020() throws Exception {
        when(processThread.getUuid()).thenReturn("111");
        when(this.openttdProcess.getProcessThread()).thenReturn(processThread);
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));

        when(openttdProcess.executeCommand(any(ServerInfoCommand.class))).thenReturn(serverInfoCommand);
        when(serverInfoCommand.getCurrentCompanies()).thenReturn(5);
        when(serverInfoCommand.isExecuted()).thenReturn(true);

        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this,"111","has started a new company"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this,"111","has joined company"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this,"111","has left the game"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this,"111","closed connection"));

        verify(this.openttdProcess,times(4)).executeCommand(any(UnpauseCommand.class));

    }
}
