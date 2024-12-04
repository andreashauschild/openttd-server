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
import org.apache.commons.lang3.StringUtils;
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
            throw new ServiceRuntimeException(e);
        }
    }

    public List<ServerFile> getOpenttdSaveGames() {
        List<ServerFile> result = new ArrayList<>();
        List<OpenttdServer> servers = this.getOpenttdServerConfig().getServers();
        try {
            FileUtils.listFiles(this.openttdSaveDirPath.toFile(), null, false).forEach(f -> {
                ServerFile saveGame = this.serverFile(f.getAbsolutePath(), SAVE_GAME);
                String serverId = saveGame.getName().split("_")[0];
                servers.stream().filter(s -> s.getId().startsWith(serverId)).findAny().ifPresent((s) -> {
                    saveGame.setOwnerId(s.getId());
                    saveGame.setOwnerName(s.getName());
                });
                result.add(saveGame);
            });
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }

    public Optional<ServerFile> getSaveGame(String fileName) {
        Path savegame = this.openttdSaveDirPath.resolve(fileName);
        if (Files.exists(savegame)) {
            return Optional.of(this.serverFile(savegame.toFile().getAbsolutePath(), SAVE_GAME));
        }
        return Optional.empty();
    }

    public Optional<ServerFile> getConfig(String fileName) {
        Path savegame = this.openttdConfigDirPath.resolve(fileName);
        if (Files.exists(savegame)) {
            return Optional.of(this.serverFile(savegame.toFile().getAbsolutePath(), CONFIG));
        }
        return Optional.empty();
    }


    public List<ServerFile> getOpenttdConfigs() {
        List<ServerFile> result = new ArrayList<>();
        try {
            FileUtils.listFiles(this.openttdConfigDirPath.toFile(), null, false).forEach(f ->
                    result.add(this.serverFile(f.getAbsolutePath(), CONFIG)));
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
            throw new ServiceRuntimeException(e);
        }
    }

    public synchronized OpenttdServer addServer(OpenttdServer server) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        Optional<OpenttdServer> first = getOpenttdServer(server.getId());
        if (!first.isPresent()) {
            throwIfPortAllocated(server.getPort(), openttdServerData.getServers());
            if(StringUtils.isNotEmpty(server.getAdminPassword())){
                throwIfAdminPortAllocated(server.getServerAdminPort(), openttdServerData.getServers());
            }
            openttdServerData.getServers().add(server);
            save(openttdServerData);
            return getOpenttdServer(server.getId()).orElse(null);
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
            OpenttdServer toUpdate = openttdServerData.getServers().get(replaceIndex);
            this.openttdServerMapper.patch(server, toUpdate);
            throwIfPortAllocated(toUpdate.getPort(), openttdServerData.getServers().stream().filter(s -> !s.getId().equals(id)).toList());
            if(StringUtils.isNotEmpty(toUpdate.getAdminPassword())){
                throwIfAdminPortAllocated(toUpdate.getServerAdminPort(), openttdServerData.getServers());
            }
            openttdServerData.getServers().set(replaceIndex, toUpdate);
            save(openttdServerData);
            return getOpenttdServer(id).get();
        } else {
            throw new ServiceRuntimeException("Can't update server. A server with name " + id + " does not exists.");
        }
    }

    public synchronized void deleteServer(String id) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        openttdServerData.setServers(openttdServerData.getServers().stream().filter(s -> !s.getId().equalsIgnoreCase(id)).toList());
        save(openttdServerData);
        if (Files.isDirectory(this.openttdConfigDirPath.resolve(id))) {
            FileUtils.deleteQuietly(this.openttdConfigDirPath.resolve(id).toFile());
        }
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
            openttdServerConfig.getServers().forEach(this::updateServerFiles);
            return openttdServerConfig;
        } catch (IOException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void updateServerFiles(OpenttdServer server) {
        if (server.getSaveGame() != null && server.getSaveGame().getPath() != null) {
            server.setSaveGame(serverFile(server.getSaveGame().getPath(), SAVE_GAME));
        }

        if (server.getOpenttdConfig() != null && server.getOpenttdConfig().getPath() != null) {
            server.setOpenttdConfig(serverFile(server.getOpenttdConfig().getPath(), CONFIG));
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

    public void throwIfAdminPortAllocated(int port, List<OpenttdServer> servers) {
        if (servers != null) {
            Optional<OpenttdServer> allocated = servers.stream().filter(s -> s.getServerAdminPort() == port).findFirst();
            // Admin port will only be active if a password is set. OpenTTD will not start a listener on this port when Admin password is not set.
            if (allocated.isPresent() && StringUtils.isNotEmpty(allocated.get().getAdminPassword())) {
                throw new ServiceRuntimeException("Error: Admin Port '" + port + " is already allocated by server '" + allocated.get().getName() + "' that have an active admin login. You must set a different port!");
            }
        }
    }

}
