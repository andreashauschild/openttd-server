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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
