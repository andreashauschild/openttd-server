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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static de.litexo.model.external.ServerFileType.CONFIG;
import static de.litexo.model.external.ServerFileType.SAVE_GAME;

@ApplicationScoped
public class DefaultRepository {
    private static final Logger LOG = Logger.getLogger(DefaultRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CONFIG_FILE_NAME = "openttd-server-config.json";
    private static final String TEMP_CONFIG_FILE_NAME = CONFIG_FILE_NAME + ".tmp";
    private static final String BACKUP_CONFIG_FILE_NAME = CONFIG_FILE_NAME + ".bak";

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

    /**
     * In memory copy of the persisted config. Every read is served from here, every successful {@link #save} replaces it.
     * Guarded by the monitor of this instance, like all other config mutations.
     */
    private InternalOpenttdServerConfig cached;

    @PostConstruct
    void init() {
        try {
            this.openttdSaveDirPath = Paths.get(this.openttdSaveDir);
            this.openttdConfigDirPath = Paths.get(this.openttdConfigDir);
            Path configDir = Paths.get(serverConfigDir);
            this.configFile = configDir.resolve(CONFIG_FILE_NAME);
            LOG.infof("server config location: '%s'", this.configFile.toFile().getAbsolutePath());
            if (!Files.isDirectory(configDir)) {
                Files.createDirectories(configDir);
            }
            // No 'Files.createFile' here, the atomic move in 'save' creates the file. An existing but empty file is
            // the result of a crash during an old, non atomic write and is replaced by a default config.
            if (!Files.exists(this.configFile) || Files.size(this.configFile) == 0) {
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
        Path savegame = validatePath(this.openttdSaveDirPath, fileName);
        if (Files.exists(savegame)) {
            return Optional.of(this.serverFile(savegame.toFile().getAbsolutePath(), SAVE_GAME));
        }
        return Optional.empty();
    }

    public Optional<ServerFile> getConfig(String fileName) {
        Path config = validatePath(this.openttdConfigDirPath, fileName);
        if (Files.exists(config)) {
            return Optional.of(this.serverFile(config.toFile().getAbsolutePath(), CONFIG));
        }
        return Optional.empty();
    }

    /**
     * Validates that the resolved path is within the allowed base directory.
     * Prevents path traversal attacks.
     */
    private Path validatePath(Path basePath, String fileName) {
        // Strip leading slashes to ensure relative resolution
        if (fileName.startsWith("/") || fileName.startsWith("\\")) {
            fileName = fileName.substring(1);
        }
        Path resolved = basePath.resolve(fileName).normalize();
        // Security check: prevent path traversal outside base directory
        if (!resolved.toAbsolutePath().startsWith(basePath.toAbsolutePath())) {
            throw new ServiceRuntimeException("Path traversal not allowed: " + fileName);
        }
        return resolved;
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


    /**
     * Writes the config to a temporary file next to the real one and moves it over the real one afterwards. That way a
     * reader or a crash (for example a SIGKILL from 'docker stop') never sees a truncated or empty config file.
     */
    public synchronized InternalOpenttdServerConfig save(InternalOpenttdServerConfig serverData) {
        try {
            Path tempFile = this.configFile.resolveSibling(TEMP_CONFIG_FILE_NAME);
            Files.writeString(tempFile, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(serverData));
            if (Files.exists(this.configFile) && Files.size(this.configFile) > 0) {
                Files.copy(this.configFile, this.configFile.resolveSibling(BACKUP_CONFIG_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(tempFile, this.configFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                LOG.debugf(e, "Atomic move is not supported for '%s', falling back to a plain replace", this.configFile);
                Files.move(tempFile, this.configFile, StandardCopyOption.REPLACE_EXISTING);
            }
            this.cached = deepCopy(serverData);
            return getOpenttdServerConfig();
        } catch (IOException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public synchronized OpenttdServer addServer(OpenttdServer server) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        Optional<OpenttdServer> first = getOpenttdServer(server.getId());
        if (!first.isPresent()) {
            // A new server is never running, no matter what the request contained
            server.setLastKnownRunning(false);
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
            // The updated server must never conflict with itself, so it is excluded from both port checks.
            List<OpenttdServer> others = openttdServerData.getServers().stream().filter(s -> !s.getId().equals(id)).toList();
            throwIfPortAllocated(toUpdate.getPort(), others);
            if(StringUtils.isNotEmpty(toUpdate.getAdminPassword())){
                throwIfAdminPortAllocated(toUpdate.getServerAdminPort(), others);
            }
            openttdServerData.getServers().set(replaceIndex, toUpdate);
            save(openttdServerData);
            return getOpenttdServer(id).get();
        } else {
            throw new ServiceRuntimeException("Can't update server. A server with name " + id + " does not exists.");
        }
    }

    /**
     * The only way to change the auto start flag. The mapper ignores it, so it can never be set by a request.
     */
    public synchronized void setLastKnownRunning(String id, boolean running) {
        InternalOpenttdServerConfig openttdServerData = getOpenttdServerConfig();
        Optional<OpenttdServer> server = openttdServerData.getServers().stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst();
        if (server.isEmpty() || server.get().isLastKnownRunning() == running) {
            return;
        }
        server.get().setLastKnownRunning(running);
        save(openttdServerData);
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

    public synchronized InternalOpenttdServerConfig getOpenttdServerConfig() {
        if (this.cached == null) {
            this.cached = readFromDisk();
        }
        // Many callers mutate the returned object before they hand it back to 'save', so every read gets its own copy.
        InternalOpenttdServerConfig openttdServerConfig = deepCopy(this.cached);
        openttdServerConfig.getServers().forEach(this::updateServerFiles);
        return openttdServerConfig;
    }

    /**
     * Drops the in memory copy so that the next read goes to disk again. Only used by tests.
     */
    synchronized void resetCache() {
        this.cached = null;
    }

    private InternalOpenttdServerConfig readFromDisk() {
        try {
            return MAPPER.readValue(this.configFile.toFile(), InternalOpenttdServerConfig.class);
        } catch (IOException e) {
            Path backupFile = this.configFile.resolveSibling(BACKUP_CONFIG_FILE_NAME);
            LOG.errorf(e, "Failed to read server config '%s'. Trying backup '%s'", this.configFile, backupFile);
            try {
                InternalOpenttdServerConfig fromBackup = MAPPER.readValue(backupFile.toFile(), InternalOpenttdServerConfig.class);
                LOG.warnf("Server config was restored from backup '%s'", backupFile);
                return fromBackup;
            } catch (IOException backupFailure) {
                // Never start with an empty config, that would silently drop the whole server list.
                throw new ServiceRuntimeException("Server config '" + this.configFile + "' is not readable and the backup '" + backupFile + "' could not be read either", backupFailure);
            }
        }
    }

    private InternalOpenttdServerConfig deepCopy(InternalOpenttdServerConfig config) {
        try {
            return MAPPER.readValue(MAPPER.writeValueAsBytes(config), InternalOpenttdServerConfig.class);
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

    public void throwIfPortAllocated(Integer port, List<OpenttdServer> servers) {
        if (port != null && servers != null) {
            Optional<OpenttdServer> allocated = servers.stream().filter(s -> Objects.equals(s.getPort(), port)).findFirst();
            if (allocated.isPresent()) {
                throw new ServiceRuntimeException("Error: Port '" + port + " is already allocated by server '" + allocated.get().getName() + "'. You must set a different port!");
            }
        }
    }

    public void throwIfAdminPortAllocated(Integer port, List<OpenttdServer> servers) {
        if (port != null && servers != null) {
            Optional<OpenttdServer> allocated = servers.stream().filter(s -> Objects.equals(s.getServerAdminPort(), port)).findFirst();
            // Admin port will only be active if a password is set. OpenTTD will not start a listener on this port when Admin password is not set.
            if (allocated.isPresent() && StringUtils.isNotEmpty(allocated.get().getAdminPassword())) {
                throw new ServiceRuntimeException("Error: Admin Port '" + port + " is already allocated by server '" + allocated.get().getName() + "' that have an active admin login. You must set a different port!");
            }
        }
    }

}
