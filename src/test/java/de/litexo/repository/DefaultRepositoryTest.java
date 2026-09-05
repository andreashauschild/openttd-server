package de.litexo.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.model.mapper.OpenttdServerMapperImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.litexo.api.ServiceRuntimeException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wildfly.common.Assert.assertNotNull;
import static org.wildfly.common.Assert.assertTrue;

@ExtendWith(MockitoExtension.class)
class DefaultRepositoryTest {

    @TempDir(cleanup = CleanupMode.ALWAYS)
    File configDir;

    File existingFile;

    File notExistingFile;

    @TempDir(cleanup = CleanupMode.ALWAYS)
    File openttdConfigDir;

    @TempDir(cleanup = CleanupMode.ALWAYS)
    File openttdSavegameDir;


    @InjectMocks
    DefaultRepository subject;

    @BeforeEach
    void beforeEach() throws IOException {
        this.subject.serverConfigDir = configDir.toString();
        this.subject.openttdSaveDir = openttdConfigDir.toString();
        this.subject.openttdConfigDir = openttdSavegameDir.toString();
        this.subject.openttdServerMapper = new OpenttdServerMapperImpl();

        this.subject.init();
        this.existingFile = Files.write(this.configDir.toPath().resolve("existingFile.txt"), "".getBytes()).toFile();
        this.notExistingFile = this.configDir.toPath().resolve("notExistingFile.txt").toFile();
    }

    @AfterEach
    void afterEach() {
        System.out.println(this.subject.configFile.toFile().getAbsolutePath());
    }

    @DisplayName("Test that config will be created if it  does not exists")
    @Test
    void test_0010() {
        InternalOpenttdServerConfig openttdServerData = this.subject.getOpenttdServerConfig();
        assertNotNull(openttdServerData);
        assertTrue(openttdServerData.getServers().isEmpty());
    }

    @DisplayName("Test adding a server")
    @Test
    void test_0020() {
        OpenttdServer created = this.subject.addServer(new OpenttdServer().setName("server1").setPort(123));
        InternalOpenttdServerConfig openttdServerData = this.subject.getOpenttdServerConfig();
        assertNotNull(created);
        assertEquals(1, openttdServerData.getServers().size());
        assertEquals("server1", openttdServerData.getServers().get(0).getName());
    }

    @DisplayName("Test serverFiles handling")
    @Test
    void test_0040() throws JsonProcessingException {

        // Check that return value of add is correcly mapped
        OpenttdServer server1 = this.subject.addServer(
                new OpenttdServer().setName("server1").setPort(123)
                        .setOpenttdConfig(new ServerFile().setPath(this.existingFile.getPath()))
                        .setSaveGame(new ServerFile().setPath(this.existingFile.getPath()))
        );

        assertEquals(true, server1.getOpenttdConfig().isExists());
        assertEquals("existingFile.txt", server1.getOpenttdConfig().getName());
        assertTrue(server1.getOpenttdConfig().getCreated() > 0);
        assertTrue(server1.getOpenttdConfig().getLastModified() > 0);

        assertEquals(true, server1.getSaveGame().isExists());
        assertEquals("existingFile.txt", server1.getSaveGame().getName());
        assertTrue(server1.getSaveGame().getCreated() > 0);
        assertTrue(server1.getSaveGame().getLastModified() > 0);

        // Check that return value of add is correcly mapped
        server1 = this.subject.getOpenttdServerConfig().getServers().get(0);

        assertEquals(true, server1.getOpenttdConfig().isExists());
        assertEquals("existingFile.txt", server1.getOpenttdConfig().getName());
        assertTrue(server1.getOpenttdConfig().getCreated() > 0);
        assertTrue(server1.getOpenttdConfig().getLastModified() > 0);

        assertEquals(true, server1.getSaveGame().isExists());
        assertEquals("existingFile.txt", server1.getSaveGame().getName());
        assertTrue(server1.getSaveGame().getCreated() > 0);
        assertTrue(server1.getSaveGame().getLastModified() > 0);

        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(server1));

    }

    @DisplayName("Test server delete")
    @Test
    void test_0050() {
        this.subject.addServer(new OpenttdServer().setName("server1").setPort(111));
        this.subject.addServer(new OpenttdServer().setId("server2-id").setName("server2").setPort(222));
        this.subject.addServer(new OpenttdServer().setName("server3").setPort(333));

        InternalOpenttdServerConfig openttdServerData = this.subject.getOpenttdServerConfig();
        assertEquals(3, openttdServerData.getServers().size());

        this.subject.deleteServer("server2-id");
        openttdServerData = this.subject.getOpenttdServerConfig();
        assertEquals(2, openttdServerData.getServers().size());
        assertEquals("server1", openttdServerData.getServers().get(0).getName());
        assertEquals("server3", openttdServerData.getServers().get(1).getName());

    }

    @DisplayName("Test server update")
    @Test
    void test_0060() {
        OpenttdServer server1 = this.subject.addServer(new OpenttdServer().setName("server1").setPort(999));


        InternalOpenttdServerConfig openttdServerData = this.subject.getOpenttdServerConfig();
        assertEquals(999, openttdServerData.getServers().get(0).getPort());

        this.subject.updateServer(server1.getId(), server1.setPort(777));
        openttdServerData = this.subject.getOpenttdServerConfig();

        assertEquals(777, openttdServerData.getServers().get(0).getPort());

    }

    @DisplayName("Test that a server with an admin password does not conflict with its own admin port on update")
    @Test
    void test_0070_updateServerWithAdminPasswordDoesNotConflictWithItself() {
        OpenttdServer server1 = this.subject.addServer(
                new OpenttdServer().setName("server1").setPort(3979).setAdminPassword("x").setServerAdminPort(3977));

        assertDoesNotThrow(() -> this.subject.updateServer(server1.getId(), server1.setName("renamed")));

        InternalOpenttdServerConfig openttdServerData = this.subject.getOpenttdServerConfig();
        assertEquals(1, openttdServerData.getServers().size());
        assertEquals("renamed", openttdServerData.getServers().get(0).getName());
        assertEquals(3977, openttdServerData.getServers().get(0).getServerAdminPort());
    }

    @DisplayName("Test that the admin port of another server having an admin login is still detected as conflict")
    @Test
    void test_0080_updateServerAdminPortConflictsWithOtherServerHavingAdminPassword() {
        this.subject.addServer(new OpenttdServer().setName("server1").setPort(3979).setAdminPassword("x").setServerAdminPort(3977));
        OpenttdServer server2 = this.subject.addServer(
                new OpenttdServer().setName("server2").setPort(3980).setAdminPassword("y").setServerAdminPort(3978));

        ServiceRuntimeException exception = assertThrows(ServiceRuntimeException.class,
                () -> this.subject.updateServer(server2.getId(), server2.setServerAdminPort(3977)));

        assertTrue(exception.getMessage().contains("Admin Port"));
    }

    @DisplayName("Test that the admin port of another server without admin login is not a conflict")
    @Test
    void test_0090_adminPortConflictIgnoredWhenOtherServerHasNoAdminPassword() {
        this.subject.addServer(new OpenttdServer().setName("server1").setPort(3979).setServerAdminPort(3977));
        OpenttdServer server2 = this.subject.addServer(
                new OpenttdServer().setName("server2").setPort(3980).setAdminPassword("y").setServerAdminPort(3978));

        assertDoesNotThrow(() -> this.subject.updateServer(server2.getId(), server2.setServerAdminPort(3977)));

        assertEquals(3977, this.subject.getOpenttdServer(server2.getId()).get().getServerAdminPort());
    }

    @DisplayName("Test that null ports do not lead to a NullPointerException")
    @Test
    void test_0100_updateServerWithNullPortsDoesNotThrow() {
        this.subject.addServer(new OpenttdServer().setName("server1").setAdminPassword("x"));
        OpenttdServer server2 = this.subject.addServer(new OpenttdServer().setName("server2"));

        assertDoesNotThrow(() -> this.subject.updateServer(server2.getId(), server2.setName("server2-renamed")));

        assertEquals("server2-renamed", this.subject.getOpenttdServer(server2.getId()).get().getName());
    }

    @DisplayName("Test that save writes atomically, keeps a backup and leaves no temp file behind")
    @Test
    void test_0110_saveIsAtomicAndLeavesNoTempFile() throws Exception {
        this.subject.addServer(new OpenttdServer().setName("server1").setPort(3979));

        Path configFilePath = this.subject.configFile;
        Path tempFilePath = configFilePath.resolveSibling("openttd-server-config.json.tmp");
        Path backupFilePath = configFilePath.resolveSibling("openttd-server-config.json.bak");

        assertFalse(Files.exists(tempFilePath));
        assertTrue(Files.exists(backupFilePath));

        InternalOpenttdServerConfig fromDisk = new ObjectMapper().readValue(configFilePath.toFile(), InternalOpenttdServerConfig.class);
        assertEquals(1, fromDisk.getServers().size());
        assertEquals("server1", fromDisk.getServers().get(0).getName());
    }

    @DisplayName("Test that a corrupt main config is recovered from the backup")
    @Test
    void test_0120_readFallsBackToBackupWhenMainConfigIsCorrupt() throws Exception {
        this.subject.addServer(new OpenttdServer().setName("server1").setPort(3979));
        this.subject.addServer(new OpenttdServer().setName("server2").setPort(3980));

        Files.writeString(this.subject.configFile, "{\"servers\": [");
        this.subject.resetCache();

        InternalOpenttdServerConfig recovered = this.subject.getOpenttdServerConfig();

        // The backup holds the state before the last save, so 'server1' must be there.
        assertEquals("server1", recovered.getServers().get(0).getName());
    }

    @DisplayName("Test that the returned config is a copy and not the cached instance")
    @Test
    void test_0130_returnedConfigIsACopy() {
        this.subject.addServer(new OpenttdServer().setName("server1").setPort(3979));

        InternalOpenttdServerConfig first = this.subject.getOpenttdServerConfig();
        first.setAutoSaveMinutes(4711);
        first.getServers().get(0).setName("mutated");
        first.getServers().clear();

        InternalOpenttdServerConfig second = this.subject.getOpenttdServerConfig();
        assertEquals(5, second.getAutoSaveMinutes());
        assertEquals(1, second.getServers().size());
        assertEquals("server1", second.getServers().get(0).getName());
    }

    @DisplayName("Test that concurrent adds and reads never fail and never lose a server")
    @Test
    void test_0140_concurrentSaveAndReadNeverThrows() throws Exception {
        int threads = 4;
        int addsPerThread = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tasks.add(() -> {
                    for (int i = 0; i < addsPerThread; i++) {
                        this.subject.addServer(new OpenttdServer().setName("concurrent"));
                        this.subject.getOpenttdServerConfig();
                    }
                    return null;
                });
            }
            List<Future<Void>> futures = executorService.invokeAll(tasks, 120, TimeUnit.SECONDS);
            for (Future<Void> future : futures) {
                // Throws if the task failed
                future.get();
            }
        } finally {
            executorService.shutdownNow();
        }

        assertEquals(threads * addsPerThread, this.subject.getOpenttdServerConfig().getServers().size());
    }
}
