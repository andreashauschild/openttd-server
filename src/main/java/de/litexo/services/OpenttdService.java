package de.litexo.services;

import de.litexo.OpenttdProcess;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.commands.Command;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.SaveCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.EventBus;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import de.litexo.model.external.ServerFileType;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
public class OpenttdService {
    private static final Logger LOG = Logger.getLogger(OpenttdService.class);
    public static final String MANUALLY_SAVE_INFIX = "_manually_save_";
    public static final String AUTO_SAVE_INFIX = "_auto_save_";
    private static final Path DUMP_BASE_DIR = Paths.get("/tmp/openttd-dumps");
    private static final long DEFAULT_SAVE_TIMEOUT_MS = 60_000;
    private static final long STOP_TIMEOUT_MS = 15_000;
    private static final long SHUTDOWN_TIMEOUT_MS = 45_000;
    private static final long SHUTDOWN_SAVE_TIMEOUT_MS = 20_000;
    private static final long SHUTDOWN_STOP_TIMEOUT_MS = 10_000;
    @ConfigProperty(name = "start-server.command")
    String startServerCommand;

    @ConfigProperty(name = "openttd.config.dir")
    String openttdConfigDir;


    @Inject
    ManagedExecutor executor;

    @Inject
    DefaultRepository repository;

    @Inject
    EventBus eventBus;

    Map<String, OpenttdProcess> processes = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    List<String> startServer = new ArrayList<>();

    @PostConstruct
    void init() {
        if (!startServerCommand.contains(";")) {
            startServer.add(startServerCommand);
        } else {
            String[] split = startServerCommand.split(";");
            for (int i = 0; i < split.length; i++) {
                startServer.add(split[i].trim());
            }
        }
        LOG.infof("start-server.command=%s", startServer);
    }

    public List<OpenttdProcess> getProcesses() {
        return new ArrayList<>(this.processes.values());
    }

    /**
     * @param processThreadUuid uuid of the {@link de.litexo.ProcessThread}, as it is reported as 'processId' in the
     *                          terminal events and in 'GET /processes'
     * @return the running process that uses that thread
     */
    public Optional<OpenttdProcess> findProcessByThreadUuid(String processThreadUuid) {
        if (processThreadUuid == null) {
            return Optional.empty();
        }
        return this.getProcesses().stream()
                .filter(process -> process.getProcessThread() != null
                        && processThreadUuid.equals(process.getProcessThread().getUuid()))
                .findAny();
    }

    public void dumpProcessData(String id, String dir) {
        if (this.processes.containsKey(id)) {
            try {
                // Strip leading slashes to ensure relative resolution
                if (dir != null && (dir.startsWith("/") || dir.startsWith("\\"))) {
                    dir = dir.substring(1);
                }
                // Default to "default" if no dir specified
                if (dir == null || dir.isBlank()) {
                    dir = "default";
                }

                Path dumpDir = DUMP_BASE_DIR.resolve(dir).normalize();

                // Security check: prevent path traversal outside dump directory
                if (!dumpDir.toAbsolutePath().startsWith(DUMP_BASE_DIR.toAbsolutePath())) {
                    throw new ServiceRuntimeException("Path traversal not allowed: " + dir);
                }

                // Create directory if it doesn't exist
                Files.createDirectories(dumpDir);

                Path dumpFile = dumpDir.resolve(id + "-" + System.currentTimeMillis() + "-dump.txt");
                FileUtils.write(dumpFile.toFile(), this.processes.get(id).getProcessThread().getLogs(), StandardCharsets.UTF_8, false);
            } catch (IOException e) {
                throw new ServiceRuntimeException(e);
            }
        } else {
            throw new ServiceRuntimeException("Process with name '" + id + "' is not running or does not exist");
        }
    }

    public Command execCommand(String processName, Command command) {
        OpenttdProcess process = this.processes.get(processName);
        if (process == null) {
            throw new ServiceRuntimeException("Process with name '" + processName + "' is not running order does not exists");
        }
        // Route through the process so that the command is serialized with the automatic commands of the schedulers.
        return process.executeCommand(command, true);
    }

    public OpenttdServer startServer(String id) {
        Optional<OpenttdServer> openttdServer = this.repository.getOpenttdServer(id);
        if (openttdServer.isEmpty()) {
            throw new ServiceRuntimeException("Failed to start server. Server with name '" + id + "' does not exists!");
        }

        OpenttdProcess running = this.processes.get(id);
        if (running != null) {
            if (running.getProcessThread() != null && running.getProcessThread().isAlive()) {
                // A second process on the same port would start and immediately fail to bind
                throw new ServiceRuntimeException("Server already running");
            }
            LOG.infof("Removing the dead process of server '%s' before it is started again", id);
            this.processes.remove(id);
        }

        OpenttdProcess openttdProcess = newOpenttdProcess();
        openttdProcess.setStartServerCommand(this.startServer);
        openttdProcess.setId(openttdServer.get().getId());
        openttdProcess.setPort(openttdServer.get().getPort());

        if (openttdServer.get().getSaveGame() != null) {
            openttdProcess.setSaveGame(openttdServer.get().getSaveGame().getPath());
        }

        handleCustomConfig(openttdServer.get(), openttdProcess);


        openttdProcess.start();
        processes.put(openttdProcess.getId(), openttdProcess);
        this.repository.setLastKnownRunning(id, true);
        return this.getOpenttdServer(openttdServer.get().getId()).orElse(null);
    }

    /**
     * Package private so that tests can hand out a mock process.
     */
    OpenttdProcess newOpenttdProcess() {
        return new OpenttdProcess(this.executor, this.eventBus);
    }

    /**
     * If a custom config is used, openttd will create many custom files in the directory of the given config.
     * To handle that behavior we do the following before we start the process:
     * 1. Create a custom directory for the given OpenttdServer in the 'openttd.config.dir'
     * 2. Create copies of the given configs move these copies to the custom directoy
     * 3. If specific values are set (like password, server name e.g) we will replace them in the copied config files
     *
     * @param openttdServer
     * @param openttdProcess
     */
    protected void handleCustomConfig(OpenttdServer openttdServer, OpenttdProcess openttdProcess) {
        try {
            Path customConfigDir = Paths.get(this.openttdConfigDir).resolve(openttdServer.getId());

            if (Files.exists(customConfigDir)) {
                FileUtils.deleteDirectory(customConfigDir.toFile());
            }
            Files.createDirectories(customConfigDir);

            Path configFile = customConfigDir.resolve("openttd.cfg");
            if (isDefined(openttdServer.getOpenttdConfig()) && Files.exists(Paths.get(openttdServer.getOpenttdConfig().getPath()))) {
                FileUtils.copyFile(new File(openttdServer.getOpenttdConfig().getPath()), configFile.toFile());

            } else {
                String defaultConfig = IOUtils.toString(this.getClass().getResourceAsStream("/templates/openttd-configs/openttd.cfg"), StandardCharsets.UTF_8);
                Files.write(configFile, defaultConfig.getBytes());
            }

            if (openttdServer.getServerAdminPort()!=null) {
                this.replaceLine(configFile, "server_admin_port", "server_admin_port = " + openttdServer.getServerAdminPort());
            }

            // Be sure that the server runs always in english, so that we can handle the terminal events like 'player joined'
            this.replaceLine(configFile, "language", "language = english_US.lng");
            openttdProcess.setConfig(configFile.toFile().getAbsolutePath());

            Path secretConfigFile = customConfigDir.resolve("secrets.cfg");
            if (!isDefined(openttdServer.getOpenttdSecretsConfig())) {
                String secretConfig = IOUtils.toString(this.getClass().getResourceAsStream("/templates/openttd-configs/secrets.cfg"), StandardCharsets.UTF_8);
                Files.write(secretConfigFile, secretConfig.getBytes());
            } else if (openttdServer.getOpenttdSecretsConfig().getPath() != null && Files.exists(Paths.get(openttdServer.getOpenttdSecretsConfig().getPath()))) {
                FileUtils.copyFile(new File(openttdServer.getOpenttdSecretsConfig().getPath()), secretConfigFile.toFile());
            }

            if (StringUtils.isNotEmpty(openttdServer.getPassword())) {
                this.replaceLine(secretConfigFile, "server_password", "server_password = " + openttdServer.getPassword());
            }

            if (StringUtils.isNotEmpty(openttdServer.getAdminPassword())) {
                this.replaceLine(secretConfigFile, "admin_password", "admin_password = " + openttdServer.getAdminPassword());
            }

            Path privateConfigFile = customConfigDir.resolve("private.cfg");
            if (!isDefined(openttdServer.getOpenttdPrivateConfig())) {
                String privateConfig = IOUtils.toString(this.getClass().getResourceAsStream("/templates/openttd-configs/private.cfg"), StandardCharsets.UTF_8);
                Files.write(privateConfigFile, privateConfig.getBytes());
            } else if (openttdServer.getOpenttdPrivateConfig().getPath() != null && Files.exists(Paths.get(openttdServer.getOpenttdPrivateConfig().getPath()))) {
                FileUtils.copyFile(new File(openttdServer.getOpenttdPrivateConfig().getPath()), privateConfigFile.toFile());
            }

            if (StringUtils.isNotEmpty(openttdServer.getName())) {
                this.replaceLine(privateConfigFile, "server_name", "server_name = " + openttdServer.getName());
            }

        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to handle custom config for server: " + openttdServer.getName());
        }
    }

    private boolean isDefined(ServerFile file) {
        return file != null && StringUtils.isNotBlank(file.getPath());
    }

    /**
     * Stops a server on request. It is not saved, the user may want to discard the current state.
     */
    public Optional<OpenttdServer> stop(String id) {
        OpenttdProcess process = this.processes.get(id);
        if (process != null) {
            process.stop(STOP_TIMEOUT_MS);
            this.processes.remove(id);

            // Read modify write instead of a partial patch object: MapStruct cannot ignore unset primitives, so a
            // patch object would reset autoSave and autoPause to their defaults on every stop.
            OpenttdServer current = this.repository.getOpenttdServer(id).orElse(null);
            if (current != null) {
                current.setInviteCode("");
                current.setCurrentClients(0);
                current.setMaxClients(0);
                current.setCurrentCompanies(0);
                current.setMaxCompanies(0);
                current.setCurrentSpectators(0);
                current.setPaused(false);
                this.repository.updateServer(id, current);
            }
            this.repository.setLastKnownRunning(id, false);
        }
        return enrich(this.repository.getOpenttdServer(id));

    }

    /**
     * Saves and stops every running server. Called from the shutdown observer, so it must not rely on the injected
     * {@link ManagedExecutor}, which may already be shutting down at that point.
     */
    public void shutdownAll() {
        List<OpenttdProcess> running = new ArrayList<>(this.processes.values());
        if (running.isEmpty()) {
            return;
        }
        long start = System.currentTimeMillis();
        LOG.infof("Saving and stopping %d running OpenTTD server(s)", running.size());
        ExecutorService shutdownExecutor = Executors.newFixedThreadPool(Math.max(1, running.size()));
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (OpenttdProcess process : running) {
                futures.add(CompletableFuture.runAsync(() -> saveAndStop(process), shutdownExecutor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.error("Not every OpenTTD server could be saved and stopped in time", e);
        } finally {
            shutdownExecutor.shutdownNow();
        }
        LOG.infof("Shutdown of the OpenTTD servers finished after %d ms", System.currentTimeMillis() - start);
    }

    private void saveAndStop(OpenttdProcess process) {
        String id = process.getId();
        try {
            OpenttdServer server = this.repository.getOpenttdServer(id).orElse(null);
            if (server != null && server.isAutoSave()) {
                autoSaveGame(id, SHUTDOWN_SAVE_TIMEOUT_MS);
            }
        } catch (Exception e) {
            LOG.error("Failed to save server '" + id + "' before the shutdown", e);
        }
        try {
            process.stop(SHUTDOWN_STOP_TIMEOUT_MS);
        } catch (Exception e) {
            LOG.error("Failed to stop server '" + id + "'", e);
        }
        // 'lastKnownRunning' stays untouched, these servers must come back on the next start
        this.processes.remove(id);
    }

    public void setTerminalOpenInUi(String id) {
        if (this.processes.containsKey(id)) {
            this.processes.get(id).setLastUiTerminalActivity(System.currentTimeMillis());
        }
    }


    public void sendTerminalCommand(String id, String cmd) {
        if (this.processes.containsKey(id) && cmd != null) {
            if (cmd.trim().equalsIgnoreCase("exit") || cmd.trim().equalsIgnoreCase("quit")) {
                this.stop(id);
            } else {
                this.processes.get(id).writeToConsole(cmd);
            }
        }
    }

    public void saveGame(String name) {
        this.createSaveGame(name, false, true, DEFAULT_SAVE_TIMEOUT_MS);
    }

    public void autoSaveGame(String name) {
        this.createSaveGame(name, true, false, DEFAULT_SAVE_TIMEOUT_MS);
    }

    /**
     * Autosave with an explicit timeout. Used on shutdown, where the whole shutdown has a budget of a few seconds.
     */
    public void autoSaveGame(String name, long timeoutMs) {
        this.createSaveGame(name, true, false, timeoutMs);
    }


    private Path getSaveGameName(String id) {
        return this.repository.getOpenttdSaveDirPath().resolve(id + MANUALLY_SAVE_INFIX + System.currentTimeMillis());
    }

    private Path getAutoSaveGameName(String id) {
        return this.repository.getOpenttdSaveDirPath().resolve(id + AUTO_SAVE_INFIX + System.currentTimeMillis());
    }

    public InternalOpenttdServerConfig getOpenttdServerConfig() {
        InternalOpenttdServerConfig openttdServerData = this.repository.getOpenttdServerConfig();
        openttdServerData.getServers().forEach(this::enrich);
        return openttdServerData;
    }

    public InternalOpenttdServerConfig save(InternalOpenttdServerConfig config) {
        return this.repository.save(config);
    }


    public Optional<OpenttdServer> getOpenttdServer(String id) {
        return enrich(this.repository.getOpenttdServer(id));
    }

    public OpenttdServer addServer(OpenttdServer server) {
        return enrich(this.repository.addServer(server.setId(UUID.randomUUID().toString())));
    }

    public OpenttdServer updateServer(String id, OpenttdServer server) {
        return enrich(this.repository.updateServer(id, server));
    }

    public void deleteServer(String id) {
        this.repository.deleteServer(id);
        stop(id);
    }


    private OpenttdServer enrich(OpenttdServer server) {
        if (server != null) {
            if (this.processes.containsKey(server.getId())) {
                server.setProcess(this.processes.get(server.getId()));
            }
            return server;
        }
        return null;
    }

    private Optional<OpenttdServer> enrich(Optional<OpenttdServer> server) {
        if (server.isPresent()) {
            enrich(server.get());
            return server;
        }
        return Optional.empty();
    }

    public List<ServerFile> getServerFiles() {
        List<ServerFile> results = new ArrayList<>();
        results.addAll(this.repository.getOpenttdSaveGames());
        results.addAll(this.repository.getOpenttdConfigs());
        return results;
    }

    /**
     * Runs 'save' on the console and only writes the new save game into the config after OpenTTD confirmed it.
     * The old implementation stored the path before the save existed and treated a file that merely appeared as
     * success, so a failed or half written save became the file that was loaded on the next start.
     */
    private void createSaveGame(String id, boolean autosave, boolean manually, long timeoutMs) {
        OpenttdServer openttdServer = this.repository.getOpenttdServer(id).orElse(null);
        OpenttdProcess process = this.processes.get(id);
        if (process == null || openttdServer == null) {
            throw new ServiceRuntimeException("Can't create save game for server '" + id + "'. Server does not exist or is not running!");
        }
        // Without this the 'save' would sit in the console of a process that is gone until the timeout expires
        if (!process.isAlive()) {
            throw new ServiceRuntimeException("Can't create save game for server '" + id + "'. The server process is not running!");
        }

        String save = null;
        if (autosave) {
            save = getAutoSaveGameName(id).toFile().getAbsolutePath();
        }
        if (manually) {
            save = getSaveGameName(id).toFile().getAbsolutePath();
        }

        // Must run even while the ui terminal is open, a save is always explicitly requested
        SaveCommand cmd = process.executeCommand(new SaveCommand(save, timeoutMs), true);

        if (cmd.isExecuted()) {
            OpenttdServer current = this.repository.getOpenttdServer(id)
                    .orElseThrow(() -> new ServiceRuntimeException("Server with id '" + id + "' disappeared while saving"));
            current.setSaveGame(this.repository.serverFile(cmd.getSavedPath(), ServerFileType.SAVE_GAME));
            this.repository.updateServer(id, current);
            LOG.infof("Save game of server '%s' written to '%s'", id, cmd.getSavedPath());
        } else if (cmd.isFailed()) {
            throw new ServiceRuntimeException("Saving map failed for server '" + id + "' - previous save game kept. Check the server terminal (disk full / not writable?)");
        } else {
            throw new ServiceRuntimeException("Save of server '" + id + "' timed out after " + timeoutMs + " ms - previous save game kept. Check the server terminal.");
        }
    }


    public Optional<OpenttdServer> pauseUnpauseServer(String id) {
        Optional<OpenttdServer> openttdServer = this.repository.getOpenttdServer(id);
        if (openttdServer.isPresent() && this.processes.containsKey(id)) {
            if (openttdServer.get().isPaused()) {
                this.processes.get(id).executeCommand(new UnpauseCommand(this.repository), true);
            } else {
                this.processes.get(id).executeCommand(new PauseCommand(this.repository), true);
            }
        }
        return enrich(this.repository.getOpenttdServer(id));
    }

    private void replaceLine(Path file, String lineContainsMatcherLowerCase, String replacement) throws IOException {
        if (file != null && lineContainsMatcherLowerCase != null && replacement != null) {
            List<String> lines = Files.readAllLines(file);
            List<String> replaced = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).toLowerCase().contains(lineContainsMatcherLowerCase)) {
                    replaced.add(replacement);
                } else {
                    replaced.add(lines.get(i));
                }
            }
            String result = replaced.stream().collect(Collectors.joining("\n"));
            Files.write(file, result.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }

    }
}
