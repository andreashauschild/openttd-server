package de.litexo.scheduler;

import de.litexo.OpenttdProcess;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.services.OpenttdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutosaveTest {

    @Mock
    OpenttdService service;

    @Mock
    OpenttdProcess firstProcess;

    @Mock
    OpenttdProcess secondProcess;

    @InjectMocks
    Autosave subject = new Autosave();

    @BeforeEach
    void beforeEach() {
        InternalOpenttdServerConfig config = new InternalOpenttdServerConfig();
        config.setAutoSaveMinutes(5);
        when(this.service.getOpenttdServerConfig()).thenReturn(config);
        lenient().when(this.firstProcess.getId()).thenReturn("first");
        lenient().when(this.secondProcess.getId()).thenReturn("second");
        // No save game yet, so an autosave is due for every server that is alive
        lenient().when(this.service.getOpenttdServer("first"))
                .thenReturn(Optional.of(new OpenttdServer().setId("first").setName("First").setAutoSave(true)));
        lenient().when(this.service.getOpenttdServer("second"))
                .thenReturn(Optional.of(new OpenttdServer().setId("second").setName("Second").setAutoSave(true)));
    }

    @DisplayName("Test that a server whose process is not running any more is not saved")
    @Test
    void test_deadProcessIsSkipped() {
        when(this.service.getProcesses()).thenReturn(List.of(this.firstProcess));
        when(this.firstProcess.isAlive()).thenReturn(false);

        this.subject.checkAutosave();

        verify(this.service, never()).autoSaveGame("first");
    }

    @DisplayName("Test that a failing save of one server does not stop the autosave of the next one")
    @Test
    void test_failureOfOneServerDoesNotStopTheOthers() {
        when(this.service.getProcesses()).thenReturn(List.of(this.firstProcess, this.secondProcess));
        when(this.firstProcess.isAlive()).thenReturn(true);
        when(this.secondProcess.isAlive()).thenReturn(true);
        doThrow(new ServiceRuntimeException("The server process is not running!")).when(this.service).autoSaveGame("first");

        this.subject.checkAutosave();

        verify(this.service).autoSaveGame("first");
        verify(this.service).autoSaveGame("second");
    }

    @DisplayName("Test that nothing is saved while the autosave interval is switched off")
    @Test
    void test_autoSaveMinutesZeroDisablesEverything() {
        InternalOpenttdServerConfig config = new InternalOpenttdServerConfig();
        config.setAutoSaveMinutes(0);
        when(this.service.getOpenttdServerConfig()).thenReturn(config);

        this.subject.checkAutosave();

        verify(this.service, never()).getProcesses();
    }

    @DisplayName("Test that a server with autosave switched off is not saved")
    @Test
    void test_autoSaveDisabledForServer() {
        when(this.service.getProcesses()).thenReturn(List.of(this.firstProcess));
        when(this.firstProcess.isAlive()).thenReturn(true);
        when(this.service.getOpenttdServer("first"))
                .thenReturn(Optional.of(new OpenttdServer().setId("first").setName("First").setAutoSave(false)));

        this.subject.checkAutosave();

        verify(this.service, never()).autoSaveGame("first");
    }
}
