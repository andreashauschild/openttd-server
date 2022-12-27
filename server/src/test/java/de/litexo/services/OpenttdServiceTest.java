package de.litexo.services;

import de.litexo.OpenttdProcess;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class OpenttdServiceTest {
    @TempDir(cleanup = CleanupMode.ALWAYS)
    File configDir;

    @InjectMocks
    OpenttdService subject;

    @BeforeEach
    void setup(){
        subject.openttdConfigDir=configDir.getAbsolutePath();
    }

    @DisplayName("Test custom openttd.cfg without secrets.cfg and private.cfg, but custom password and name")
    @Test
    void test_0001() throws Exception {
        Path openttdConfig = configDir.toPath().resolve("openttd.cfg");
        Files.write(openttdConfig, "".getBytes());
        OpenttdServer server = new OpenttdServer().setId("id1").setName("Name1").setPassword("Password_1");
        server.setOpenttdConfig(new ServerFile().setPath(openttdConfig.toFile().getAbsolutePath()));
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
}