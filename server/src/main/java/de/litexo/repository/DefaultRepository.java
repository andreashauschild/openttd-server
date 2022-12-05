package de.litexo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.model.OpenttdServer;
import de.litexo.model.OpenttdServerConfig;
import de.litexo.model.ServerFile;
import de.litexo.model.ServerFileType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.litexo.model.ServerFileType.*;

@ApplicationScoped
public class DefaultRepository {
    private static final Logger LOG = Logger.getLogger(DefaultRepository.class);

    @ConfigProperty(name = "server.config.dir")
    String serverConfigDir;

    @ConfigProperty(name = "openttd.save.dir")
    String openttdSaveDir;

    @ConfigProperty(name = "openttd.config.dir")
    String openttdConfigDir;

    Path configFile;

    Path openttdSaveDirPath;

    Path openttdConfigDirPath;

    @PostConstruct
    void init() {
        try {
            this.openttdSaveDirPath = Paths.get(this.openttdSaveDir);
            this.openttdConfigDirPath = Paths.get(this.openttdConfigDir);
            Path configDir = Paths.get(serverConfigDir);
            this.configFile = configDir.resolve("openttd-server-config.json");
            LOG.infof("server config location: '%s'", this.configFile.toFile().getAbsolutePath());
            if (!Files.isDirectory(configDir)) {
                Files.createDirectories(configDir);
                Files.createFile(this.configFile);
                this.save(new OpenttdServerConfig());
            } else if (!Files.exists(this.configFile)) {
                Files.createFile(this.configFile);
                this.save(new OpenttdServerConfig());
            }

            if (!Files.isDirectory(this.openttdSaveDirPath)) {
                Files.createDirectories(this.openttdSaveDirPath);
            }

            if (!Files.isDirectory(this.openttdConfigDirPath)) {
                Files.createDirectories(this.openttdConfigDirPath);
            }

            LOG.infof("oppenttd config location: '%s'", this.openttdConfigDirPath.toFile().getAbsolutePath());
            LOG.infof("oppenttd save game location: '%s'", this.openttdSaveDirPath.toFile().getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ServerFile> getOpenttdSaveGames() {
        List<ServerFile> result = new ArrayList<>();
        try {
            FileUtils.listFiles(this.openttdSaveDirPath.toFile(), null, false).forEach(f -> {
                result.add(this.serverFile(f.getAbsolutePath(), SAVE_GAME));
            });
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }

    public List<ServerFile> getOpenttdConfigs() {
        List<ServerFile> result = new ArrayList<>();
        try {
            FileUtils.listFiles(this.openttdConfigDirPath.toFile(), null, false).forEach(f -> {
                result.add(this.serverFile(f.getAbsolutePath(),CONFIG));
            });
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }


    public synchronized OpenttdServerConfig save(OpenttdServerConfig serverData) {
        try {
            Files.writeString(this.configFile, new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(serverData), StandardOpenOption.TRUNCATE_EXISTING);
            return getOpenttdServerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized OpenttdServer addServer(OpenttdServer server) {
        OpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        Optional<OpenttdServer> first = getOpenttdServer(server.getName());
        if (!first.isPresent()) {
            openttdServerData.getServers().add(server);
            save(openttdServerData);
            return getOpenttdServer(server.getName()).get();
        } else {
            throw new ServiceRuntimeException("Can't add server. A server with name " + server.getName() + " already exists.");
        }
    }

    public synchronized OpenttdServer updateServer(OpenttdServer server) {
        OpenttdServerConfig openttdServerData = getOpenttdServerConfig();

        int replaceIndex = -1;
        for (int i = 0; i < openttdServerData.getServers().size(); i++) {
            if (openttdServerData.getServers().get(i).getName().equalsIgnoreCase(server.getName())) {
                replaceIndex = i;
                break;
            }
        }

        if (replaceIndex > -1) {
            openttdServerData.getServers().set(replaceIndex, server);
            save(openttdServerData);
            return getOpenttdServer(server.getName()).get();
        } else {
            throw new ServiceRuntimeException("Can't update server. A server with name " + server.getName() + " does not exists.");
        }
    }

    public synchronized void deleteServer(String name) {
        OpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        openttdServerData.setServers(openttdServerData.getServers().stream().filter(s -> !s.getName().equalsIgnoreCase(name)).collect(Collectors.toList()));
        save(openttdServerData);
    }


    public Optional<OpenttdServer> getOpenttdServer(String name) {
        OpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        Optional<OpenttdServer> first = openttdServerData.getServers().stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst();
        if (first.isPresent()) {
            updateServerFiles(first.get());
        }
        return first;
    }

    public OpenttdServerConfig getOpenttdServerConfig() {
        try {
            OpenttdServerConfig openttdServerConfig = new ObjectMapper().readValue(this.configFile.toFile(), OpenttdServerConfig.class);
            openttdServerConfig.getServers().forEach(s -> updateServerFiles(s));
            return openttdServerConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateServerFiles(OpenttdServer server) {
        if (server.getStartSaveGame() != null && server.getStartSaveGame().getPath() != null) {
            server.setStartSaveGame(serverFile(server.getStartSaveGame().getPath(),SAVE_GAME));
        }

        if (server.getAutoSaveGame() != null && server.getAutoSaveGame().getPath() != null) {
            server.setAutoSaveGame(serverFile(server.getAutoSaveGame().getPath(),SAVE_GAME));
        }

        if (server.getSaveGame() != null && server.getSaveGame().getPath() != null) {
            server.setSaveGame(serverFile(server.getSaveGame().getPath(),SAVE_GAME));
        }

        if (server.getConfig() != null && server.getConfig().getPath() != null) {
            server.setConfig(serverFile(server.getConfig().getPath(), CONFIG));
        }

    }

    public ServerFile serverFile(String path, ServerFileType type) {
        try {
            ServerFile serverFile = new ServerFile().setPath(path).setName(FilenameUtils.getName(path)).setType(type);
            if (Files.exists(Paths.get(serverFile.getPath()))) {
                BasicFileAttributes attr = Files.readAttributes(Paths.get(serverFile.getPath()), BasicFileAttributes.class);
                serverFile.setCreated(attr.creationTime().toMillis());
                serverFile.setLastModified(attr.lastModifiedTime().toMillis());
            } else {
                serverFile.setExists(false);
            }

            return serverFile;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Path getOpenttdSaveDirPath() {
        return openttdSaveDirPath;
    }

    public Path getOpenttdConfigDirPath() {
        return openttdConfigDirPath;
    }
}
