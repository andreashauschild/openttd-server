package de.litexo.scheduler;

import de.litexo.OpenttdProcess;
import de.litexo.commands.Command;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.ServerInfoCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;
import de.litexo.services.OpenttdService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;

@ExtendWith(MockitoExtension.class)
class AutoPauseUnpauseTest {


    @Mock
    OpenttdService service;

    @Mock
    OpenttdProcess openttdProcess;

    @Mock
    ServerInfoCommand serverInfoCommand;

    @Mock
    DefaultRepository repository;

    @Mock
    UpdateServerInfo updateServerInfo;

    @InjectMocks
    AutoPauseUnpause subject = new AutoPauseUnpause();

    @BeforeEach
    void beforeEach() {
        // A dead process is skipped without sending a command, every case here is about a running one
        lenient().when(this.openttdProcess.isAlive()).thenReturn(true);
    }

    @DisplayName("Test pause because no client connected")
    @Test
    void test001() throws Exception {
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(new OpenttdServer().setAutoPause(true)));
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(PauseCommand.class), anyBoolean())).thenAnswer(a-> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument,"executed",true,true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(5);
        when(serverInfoCommand.getCurrentSpectators()).thenReturn(5);
        when(serverInfoCommand.isExecuted()).thenReturn(true);
        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess).executeCommand(any(PauseCommand.class), anyBoolean());

    }

    @DisplayName("Test unpause because client connected to company")
    @Test
    void test010() throws Exception {
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(new OpenttdServer().setAutoPause(true)));
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(UnpauseCommand.class), anyBoolean())).thenAnswer(a-> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument,"executed",true,true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(7);
        when(serverInfoCommand.getCurrentSpectators()).thenReturn(5);
        when(serverInfoCommand.isExecuted()).thenReturn(true);
        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess).executeCommand(any(UnpauseCommand.class), anyBoolean());

    }

    @DisplayName("Test handleTerminalUpdateEvent")
    @Test
    void test020() throws Exception {
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(new OpenttdServer().setAutoPause(true)));
        when(this.service.findProcessByThreadUuid("111")).thenReturn(Optional.of(this.openttdProcess));

        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(UnpauseCommand.class), anyBoolean())).thenAnswer(a-> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument,"executed",true,true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(7);
        when(serverInfoCommand.getCurrentSpectators()).thenReturn(5);
        when(serverInfoCommand.isExecuted()).thenReturn(true);

        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has started a new company"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has joined company"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "has left the game"));
        this.subject.handleTerminalUpdateEvent(new OpenttdTerminalUpdateEvent(this, "111", "closed connection"));

        verify(this.openttdProcess, times(4)).executeCommand(any(UnpauseCommand.class), anyBoolean());

    }

    @DisplayName("Test that the server info is taken from the same 'server_info' result instead of a second command")
    @Test
    void test030_serverInfoAppliedFromSameCommandResult() throws Exception {
        OpenttdServer server = new OpenttdServer().setAutoPause(true);
        when(this.service.getOpenttdServer(any())).thenReturn(Optional.of(server));
        when(this.service.getProcesses()).thenReturn(List.of(openttdProcess));
        when(openttdProcess.executeCommand(any(ServerInfoCommand.class), anyBoolean())).thenReturn(serverInfoCommand);
        when(openttdProcess.executeCommand(any(PauseCommand.class), anyBoolean())).thenAnswer(a -> {
            Command argument = (Command) a.getArgument(0);
            FieldUtils.writeField(argument, "executed", true, true);
            return argument;
        });
        when(serverInfoCommand.getCurrentClients()).thenReturn(0);
        when(serverInfoCommand.getCurrentSpectators()).thenReturn(0);
        when(serverInfoCommand.isExecuted()).thenReturn(true);

        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess, times(1)).executeCommand(any(ServerInfoCommand.class), anyBoolean());
        verify(this.updateServerInfo, times(1)).applyServerInfo(eq(server), eq(this.serverInfoCommand));
        Assertions.assertTrue(server.isPaused());
    }

    @DisplayName("Test that a process that is not running any more is not asked for its server info")
    @Test
    void test040_deadProcessIsSkipped() {
        when(this.service.getProcesses()).thenReturn(List.of(this.openttdProcess));
        when(this.openttdProcess.isAlive()).thenReturn(false);

        this.subject.checkAutoPauseUnpause();

        verify(this.openttdProcess, never()).executeCommand(any(), anyBoolean());
    }

}
