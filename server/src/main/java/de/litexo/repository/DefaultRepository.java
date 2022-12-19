package de.litexo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import de.litexo.model.external.ServerFileType;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.model.mapper.OpenttdServerMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

import static de.litexo.model.external.ServerFileType.CONFIG;
import static de.litexo.model.external.ServerFileType.SAVE_GAME;

@ApplicationScoped
public class DefaultRepository {
    private static final Logger LOG = Logger.getLogger(DefaultRepository.class);

    @ConfigProperty(name = "server.config.dir")
    String serverConfigDir;

    @ConfigProperty(name = "openttd.save.dir")
    String openttdSaveDir;

    @ConfigProperty(name = "openttd.config.dir")
    String openttdConfigDir;

    @Inject
    OpenttdServerMapper openttdServerMapper;

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
                this.save(new InternalOpenttdServerConfig());
            } else if (!Files.exists(this.configFile)) {
                Files.createFile(this.configFile);
                this.save(new InternalOpenttdServerConfig());
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
                result.add(this.serverFile(f.getAbsolutePath(), CONFIG));
            });
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }


    public synchronized InternalOpenttdServerConfig save(InternalOpenttdServerConfig serverData) {
        try {
            Files.writeString(this.configFile, new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(serverData), StandardOpenOption.TRUNCATE_EXISTING);
            return getOpenttdServerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized OpenttdServer addServer(OpenttdServer server) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        Optional<OpenttdServer> first = getOpenttdServer(server.getId());
        if (!first.isPresent()) {
            throwIfPortAllocated(server.getPort(), openttdServerData.getServers());
            openttdServerData.getServers().add(server);
            save(openttdServerData);
            return getOpenttdServer(server.getId()).get();
        } else {
            throw new ServiceRuntimeException("Can't add server. A server with id " + server.getId() + " already exists.");
        }
    }

    public synchronized OpenttdServer updateServer(String id, OpenttdServer server) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();

        int replaceIndex = -1;
        for (int i = 0; i < openttdServerData.getServers().size(); i++) {
            if (openttdServerData.getServers().get(i).getId().equalsIgnoreCase(id)) {
                replaceIndex = i;
                break;
            }
        }

        if (replaceIndex > -1) {
            throwIfPortAllocated(server.getPort(), openttdServerData.getServers().stream().filter(s -> !s.getId().equals(id)).collect(Collectors.toList()));
            OpenttdServer toUpdate = openttdServerData.getServers().get(replaceIndex);
            this.openttdServerMapper.patch(server, toUpdate);
            openttdServerData.getServers().set(replaceIndex, toUpdate);
            save(openttdServerData);
            return getOpenttdServer(id).get();
        } else {
            throw new ServiceRuntimeException("Can't update server. A server with name " + id + " does not exists.");
        }
    }

    public synchronized void deleteServer(String id) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        openttdServerData.setServers(openttdServerData.getServers().stream().filter(s -> !s.getId().equalsIgnoreCase(id)).collect(Collectors.toList()));
        save(openttdServerData);
    }


    public Optional<OpenttdServer> getOpenttdServer(String id) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        Optional<OpenttdServer> first = openttdServerData.getServers().stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst();
        if (first.isPresent()) {
            updateServerFiles(first.get());
        }
        return first;
    }

    public InternalOpenttdServerConfig getOpenttdServerConfig() {
        try {
            InternalOpenttdServerConfig openttdServerConfig = new ObjectMapper().readValue(this.configFile.toFile(), InternalOpenttdServerConfig.class);
            openttdServerConfig.getServers().forEach(s -> updateServerFiles(s));
            return openttdServerConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateServerFiles(OpenttdServer server) {
        if (server.getSaveGame() != null && server.getSaveGame().getPath() != null) {
            server.setSaveGame(serverFile(server.getSaveGame().getPath(), SAVE_GAME));
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

    public void throwIfPortAllocated(int port, List<OpenttdServer> servers) {
        if (servers != null) {
            Optional<OpenttdServer> allocated = servers.stream().filter(s -> s.getPort() == port).findFirst();
            if (allocated.isPresent()) {
                throw new ServiceRuntimeException("Error: Port '" + port + " is already allocated by server '" + allocated.get().getName() + "'. You must set a different port!");
            }
        }
    }

}
