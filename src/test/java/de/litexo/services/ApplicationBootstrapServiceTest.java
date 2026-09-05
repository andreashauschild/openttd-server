package de.litexo.services;

import de.litexo.OpenttdProcess;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationBootstrapServiceTest {

    @Mock
    DefaultRepository repository;

    @Mock
    OpenttdService service;

    @InjectMocks
    ApplicationBootstrapService subject = new ApplicationBootstrapService();

    private InternalOpenttdServerConfig config(OpenttdServer... servers) {
        InternalOpenttdServerConfig config = new InternalOpenttdServerConfig();
        config.setServers(List.of(servers));
        return config;
    }

    @DisplayName("Test that only the servers that were running are started again")
    @Test
    void test_onStartRestartsOnlyLastKnownRunningServers() {
        OpenttdServer stopped = new OpenttdServer().setId("stopped").setName("stopped");
        OpenttdServer running = new OpenttdServer().setId("running").setName("running");
        running.setLastKnownRunning(true);
        this.subject.autoStartRunningServers = true;
        when(this.repository.getOpenttdServerConfig()).thenReturn(config(stopped, running));

        this.subject.onStart(new StartupEvent());

        verify(this.service).startServer("running");
        verify(this.service, never()).startServer("stopped");
    }

    @DisplayName("Test that a server that fails to start does not stop the remaining ones")
    @Test
    void test_onStartContinuesAfterAFailure() {
        OpenttdServer first = new OpenttdServer().setId("first").setName("first");
        first.setLastKnownRunning(true);
        OpenttdServer second = new OpenttdServer().setId("second").setName("second");
        second.setLastKnownRunning(true);
        this.subject.autoStartRunningServers = true;
        when(this.repository.getOpenttdServerConfig()).thenReturn(config(first, second));
        when(this.service.startServer("first")).thenThrow(new ServiceRuntimeException("boom"));

        this.subject.onStart(new StartupEvent());

        verify(this.service).startServer("first");
        verify(this.service).startServer("second");
    }

    @DisplayName("Test that the auto start can be switched off")
    @Test
    void test_onStartDisabledByProperty() {
        this.subject.autoStartRunningServers = false;

        this.subject.onStart(new StartupEvent());

        verify(this.repository, never()).getOpenttdServerConfig();
        verify(this.service, never()).startServer(anyString());
    }

    @DisplayName("Test that the shutdown is delegated to the service")
    @Test
    void test_onStopSavesAutoSaveServersThenStops() {
        this.subject.onStop(new ShutdownEvent());

        verify(this.service).shutdownAll();
    }
}
