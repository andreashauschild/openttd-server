package de.litexo.services;

import de.litexo.OpenttdProcess;
import de.litexo.ProcessThread;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.commands.SaveCommand;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import de.litexo.repository.DefaultRepository;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenttdServiceTest {
    @TempDir(cleanup = CleanupMode.ALWAYS)
    File configDir;

    @TempDir(cleanup = CleanupMode.ALWAYS)
    File saveGameDir;

    @Mock
    DefaultRepository repository;

    @Mock
    OpenttdProcess openttdProcess;

    @Mock
    ProcessThread processThread;

    @InjectMocks
    OpenttdService subject;

    @BeforeEach
    void setup(){
        subject.openttdConfigDir=configDir.getAbsolutePath();
    }

    private OpenttdService withMockedProcess(String id) {
        OpenttdService spy = Mockito.spy(this.subject);
        doReturn(this.openttdProcess).when(spy).newOpenttdProcess();
        when(this.openttdProcess.getId()).thenReturn(id);
        return spy;
    }

    @DisplayName("Test custom openttd.cfg without secrets.cfg and private.cfg, but custom password and name")
    @Test
    void test_0001() throws Exception {
        Path openttdConfig = configDir.toPath().resolve("openttd.cfg");
        Files.write(openttdConfig, "".getBytes());
        OpenttdServer server = new OpenttdServer().setId("id1").setName("Name1").setPassword("Password_1").setAdminPassword("Admin_1234");
        server.setOpenttdConfig(new ServerFile().setPath(openttdConfig.toFile().getAbsolutePath()));
        OpenttdProcess process = new OpenttdProcess();

        this.subject.handleCustomConfig(server, process);


        // check that openttd.cfg was written to custom server config directory
        assertEquals(configDir.toPath().resolve("id1").resolve("openttd.cfg").toFile().getAbsolutePath(), process.getConfig());

        String privateCfg = Files.readString(configDir.toPath().resolve("id1").resolve("private.cfg"));
        String secretCfg = Files.readString(configDir.toPath().resolve("id1").resolve("secrets.cfg"));

        // Check that custom password was set to secrets.cfg
        assertTrue(secretCfg.contains("server_password = Password_1\n") || secretCfg.contains("server_password = Password_1\r"));
        assertTrue(secretCfg.contains("admin_password = Admin_1234\n") || secretCfg.contains("server_password = Admin_1234\r"));

        // Check that custom server name was set to private.cfg
        assertTrue(privateCfg.contains("server_name = Name1\n") || privateCfg.contains("server_name = Name1\r"));
    }

    @DisplayName("Test custom openttd.cfg without secrets.cfg and private.cfg. No custom name and password")
    @Test
    void test_0010() throws Exception {
        Path openttdConfig = configDir.toPath().resolve("openttd.cfg");
        Files.write(openttdConfig, "".getBytes());
        OpenttdServer server = new OpenttdServer().setId("id1");
        server.setOpenttdConfig(new ServerFile().setPath(openttdConfig.toFile().getAbsolutePath()));
        OpenttdProcess process = new OpenttdProcess();

        this.subject.handleCustomConfig(server, process);


        // check that openttd.cfg was written to custom server config directory
        assertEquals(configDir.toPath().resolve("id1").resolve("openttd.cfg").toFile().getAbsolutePath(), process.getConfig());

        String privateCfg = Files.readString(configDir.toPath().resolve("id1").resolve("private.cfg"));
        String secretCfg = Files.readString(configDir.toPath().resolve("id1").resolve("secrets.cfg"));

        // Check that default password was set to secrets.cfg
        assertTrue(secretCfg.contains("server_password =\n") || secretCfg.contains("server_password =\r"));

        // Check that default server name was set to private.cfg
        assertTrue(privateCfg.contains("server_name = unknown server\n") || privateCfg.contains("server_name = unknown server\r"));
    }

    @DisplayName("Test custom openttd.cfg without custom secrets.cfg and private.cfg. No custom name and password")
    @Test
    void test_0030() throws Exception {
        Path openttdConfig = configDir.toPath().resolve("openttd.cfg");
        Path openttdSecret = configDir.toPath().resolve("customOpenTTDsecrets.cfg");
        Path openttdPrivate = configDir.toPath().resolve("customOpenTTDPrivate.cfg");
        Files.write(openttdConfig, "".getBytes());
        Files.write(openttdSecret, "server_password = 123\n".getBytes());
        Files.write(openttdPrivate, "server_name = xxx\n".getBytes());
        OpenttdServer server = new OpenttdServer().setId("id1");
        server.setOpenttdConfig(new ServerFile().setPath(openttdConfig.toFile().getAbsolutePath()));
        server.setOpenttdSecretsConfig(new ServerFile().setPath(openttdSecret.toFile().getAbsolutePath()));
        server.setOpenttdPrivateConfig(new ServerFile().setPath(openttdPrivate.toFile().getAbsolutePath()));
        OpenttdProcess process = new OpenttdProcess();

        this.subject.handleCustomConfig(server, process);


        // check that openttd.cfg was written to custom server config directory
        assertEquals(configDir.toPath().resolve("id1").resolve("openttd.cfg").toFile().getAbsolutePath(), process.getConfig());

        String privateCfg = Files.readString(configDir.toPath().resolve("id1").resolve("private.cfg"));
        String secretCfg = Files.readString(configDir.toPath().resolve("id1").resolve("secrets.cfg"));

        // Check that default password was set to secrets.cfg
        assertTrue(secretCfg.contains("server_password = 123\n") || secretCfg.contains("server_password = 123\r"));

        // Check that default server name was set to private.cfg
        assertTrue(privateCfg.contains("server_name = xxx\n") || privateCfg.contains("server_name = xxx\r"));
    }

    @DisplayName("Test custom openttd.cfg without custom secrets.cfg and private.cfg. With custom name and password")
    @Test
    void test_0040() throws Exception {
        Path openttdConfig = configDir.toPath().resolve("openttd.cfg");
        Path openttdSecret = configDir.toPath().resolve("customOpenTTDsecrets.cfg");
        Path openttdPrivate = configDir.toPath().resolve("customOpenTTDPrivate.cfg");
        Files.write(openttdConfig, "".getBytes());
        Files.write(openttdSecret, "server_password = 123\n bla = 1123\n".getBytes());
        Files.write(openttdPrivate, "server_name = xxx\n  bla = 1123\n".getBytes());
        OpenttdServer server = new OpenttdServer().setId("id1").setName("Name1").setPassword("Password_1");
        server.setOpenttdConfig(new ServerFile().setPath(openttdConfig.toFile().getAbsolutePath()));
        server.setOpenttdSecretsConfig(new ServerFile().setPath(openttdSecret.toFile().getAbsolutePath()));
        server.setOpenttdPrivateConfig(new ServerFile().setPath(openttdPrivate.toFile().getAbsolutePath()));
        OpenttdProcess process = new OpenttdProcess();

        this.subject.handleCustomConfig(server, process);


        // check that openttd.cfg was written to custom server config directory
        assertEquals(configDir.toPath().resolve("id1").resolve("openttd.cfg").toFile().getAbsolutePath(), process.getConfig());

        String privateCfg = Files.readString(configDir.toPath().resolve("id1").resolve("private.cfg"));
        String secretCfg = Files.readString(configDir.toPath().resolve("id1").resolve("secrets.cfg"));

        // Check that custom password was set to secrets.cfg
        assertTrue(secretCfg.contains("server_password = Password_1\n") || secretCfg.contains("server_password = Password_1\r"));

        // Check that custom server name was set to private.cfg
        assertTrue(privateCfg.contains("server_name = Name1\n") || privateCfg.contains("server_name = Name1\r"));
    }

    @DisplayName("Test that starting a server records it as running")
    @Test
    void test_0100_startServerSetsLastKnownRunningTrue() throws Exception {
        String id = "id1";
        Path openttdConfig = configDir.toPath().resolve("openttd.cfg");
        Files.write(openttdConfig, "".getBytes());
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1").setPort(3979);
        server.setOpenttdConfig(new ServerFile().setPath(openttdConfig.toFile().getAbsolutePath()));

        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        OpenttdService spy = withMockedProcess(id);

        spy.startServer(id);

        verify(this.openttdProcess).start();
        verify(this.repository).setLastKnownRunning(id, true);
    }

    @DisplayName("Test that stopping a server keeps the auto flags and clears the running state")
    @Test
    void test_0110_stopServerSetsLastKnownRunningFalseAndKeepsAutoFlags() {
        String id = "id1";
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1").setAutoSave(false).setAutoPause(false);
        server.setPaused(true);
        server.setCurrentClients(3);
        server.setMaxClients(25);
        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        this.subject.processes.put(id, this.openttdProcess);

        this.subject.stop(id);

        verify(this.openttdProcess).stop(15_000L);
        ArgumentCaptor<OpenttdServer> captor = ArgumentCaptor.forClass(OpenttdServer.class);
        verify(this.repository).updateServer(eq(id), captor.capture());
        OpenttdServer updated = captor.getValue();
        assertEquals(false, updated.isAutoSave());
        assertEquals(false, updated.isAutoPause());
        assertEquals(false, updated.isPaused());
        assertEquals(0, updated.getCurrentClients());
        assertEquals(0, updated.getMaxClients());
        assertEquals("", updated.getInviteCode());
        verify(this.repository).setLastKnownRunning(id, false);
        assertTrue(this.subject.processes.isEmpty());
    }

    @DisplayName("Test that a running server cannot be started a second time")
    @Test
    void test_0120_startServerTwiceThrowsWhileRunning() {
        String id = "id1";
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1").setPort(3979);
        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        when(this.openttdProcess.getProcessThread()).thenReturn(this.processThread);
        when(this.processThread.isAlive()).thenReturn(true);
        this.subject.processes.put(id, this.openttdProcess);

        ServiceRuntimeException exception = assertThrows(ServiceRuntimeException.class, () -> this.subject.startServer(id));
        assertTrue(exception.getMessage().contains("already running"));
    }

    @DisplayName("Test that the save game of the config is only replaced after OpenTTD confirmed the save")
    @Test
    void test_0130_saveGameUpdatesConfigOnlyAfterSuccess() {
        String id = "id1";
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1");
        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        when(this.repository.getOpenttdSaveDirPath()).thenReturn(this.saveGameDir.toPath());
        when(this.repository.serverFile(anyString(), any())).thenAnswer(a -> new ServerFile().setPath(a.getArgument(0)));
        when(this.openttdProcess.isAlive()).thenReturn(true);
        when(this.openttdProcess.executeCommand(any(SaveCommand.class), anyBoolean())).thenAnswer(a -> {
            SaveCommand cmd = a.getArgument(0);
            FieldUtils.writeField(cmd, "executed", true, true);
            FieldUtils.writeField(cmd, "savedPath", this.saveGameDir.toPath().resolve("id1_manually_save_1.sav").toString(), true);
            return cmd;
        });
        this.subject.processes.put(id, this.openttdProcess);

        this.subject.saveGame(id);

        ArgumentCaptor<OpenttdServer> captor = ArgumentCaptor.forClass(OpenttdServer.class);
        verify(this.repository).updateServer(eq(id), captor.capture());
        assertEquals(this.saveGameDir.toPath().resolve("id1_manually_save_1.sav").toString(), captor.getValue().getSaveGame().getPath());
    }

    @DisplayName("Test that a failed save keeps the previous save game and reports an error")
    @Test
    void test_0140_saveGameFailureKeepsPreviousSaveAndThrows() {
        String id = "id1";
        ServerFile previousSave = new ServerFile().setPath("/save/previous.sav");
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1").setSaveGame(previousSave);
        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        when(this.repository.getOpenttdSaveDirPath()).thenReturn(this.saveGameDir.toPath());
        when(this.openttdProcess.isAlive()).thenReturn(true);
        when(this.openttdProcess.executeCommand(any(SaveCommand.class), anyBoolean())).thenAnswer(a -> {
            SaveCommand cmd = a.getArgument(0);
            FieldUtils.writeField(cmd, "failed", true, true);
            FieldUtils.writeField(cmd, "failureReason", "Saving map failed", true);
            return cmd;
        });
        this.subject.processes.put(id, this.openttdProcess);

        ServiceRuntimeException exception = assertThrows(ServiceRuntimeException.class, () -> this.subject.saveGame(id));

        assertTrue(exception.getMessage().contains("Saving map failed"));
        verify(this.repository, never()).updateServer(anyString(), any());
        assertEquals(previousSave, server.getSaveGame());
    }

    @DisplayName("Test that the shutdown saves a server with autosave before it stops it")
    @Test
    void test_0150_shutdownAllSavesAutoSaveServersThenStops() {
        String id = "id1";
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1").setAutoSave(true);
        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        when(this.repository.getOpenttdSaveDirPath()).thenReturn(this.saveGameDir.toPath());
        when(this.repository.serverFile(anyString(), any())).thenAnswer(a -> new ServerFile().setPath(a.getArgument(0)));
        when(this.openttdProcess.getId()).thenReturn(id);
        when(this.openttdProcess.isAlive()).thenReturn(true);
        when(this.openttdProcess.executeCommand(any(SaveCommand.class), anyBoolean())).thenAnswer(a -> {
            SaveCommand cmd = a.getArgument(0);
            FieldUtils.writeField(cmd, "executed", true, true);
            FieldUtils.writeField(cmd, "savedPath", this.saveGameDir.toPath().resolve("id1_auto_save_1.sav").toString(), true);
            return cmd;
        });
        this.subject.processes.put(id, this.openttdProcess);

        this.subject.shutdownAll();

        InOrder inOrder = Mockito.inOrder(this.openttdProcess);
        inOrder.verify(this.openttdProcess).executeCommand(any(SaveCommand.class), anyBoolean());
        inOrder.verify(this.openttdProcess).stop(10_000L);
        // The auto start flag must survive a shutdown
        verify(this.repository, never()).setLastKnownRunning(anyString(), anyBoolean());
        assertTrue(this.subject.processes.isEmpty());
    }

    @DisplayName("Test that the shutdown does not save a server with autosave switched off")
    @Test
    void test_0160_shutdownAllDoesNotSaveWhenAutoSaveIsOff() {
        String id = "id1";
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1").setAutoSave(false);
        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        when(this.openttdProcess.getId()).thenReturn(id);
        this.subject.processes.put(id, this.openttdProcess);

        this.subject.shutdownAll();

        verify(this.openttdProcess, never()).executeCommand(any(SaveCommand.class), anyBoolean());
        verify(this.openttdProcess).stop(10_000L);
    }

    @DisplayName("Test that a save of a server whose process died fails right away instead of waiting for the timeout")
    @Test
    void test_0170_saveGameOnDeadProcessThrowsWithoutSendingACommand() {
        String id = "id1";
        ServerFile previousSave = new ServerFile().setPath("/save/previous.sav");
        OpenttdServer server = new OpenttdServer().setId(id).setName("Name1").setSaveGame(previousSave);
        when(this.repository.getOpenttdServer(id)).thenReturn(Optional.of(server));
        when(this.openttdProcess.isAlive()).thenReturn(false);
        this.subject.processes.put(id, this.openttdProcess);

        ServiceRuntimeException exception = assertThrows(ServiceRuntimeException.class, () -> this.subject.saveGame(id));

        assertTrue(exception.getMessage().contains("process is not running"));
        verify(this.openttdProcess, never()).executeCommand(any(SaveCommand.class), anyBoolean());
        verify(this.repository, never()).updateServer(anyString(), any());
        assertEquals(previousSave, server.getSaveGame());
    }

}
