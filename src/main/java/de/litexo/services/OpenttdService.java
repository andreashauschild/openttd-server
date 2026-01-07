package de.litexo.services;

import de.litexo.OpenttdProcess;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.commands.Command;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.QuitCommand;
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
import java.util.stream.Collectors;

@ApplicationScoped
public class OpenttdService {
    public static final String MANUALLY_SAVE_INFIX = "_manually_save_";
    public static final String AUTO_SAVE_INFIX = "_auto_save_";
    private static final Path DUMP_BASE_DIR = Paths.get("/tmp/openttd-dumps");
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
        System.out.println("start-server.command=" + startServer);
    }

    public List<OpenttdProcess> getProcesses() {
        return new ArrayList<>(this.processes.values());
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
        if (this.processes.containsKey(processName)) {
            return command.execute(this.processes.get(processName).getProcessThread(), this.processes.get(processName).getId());
        } else {
            throw new ServiceRuntimeException("Process with name '" + processName + "' is not running order does not exists");
        }
    }

    public OpenttdServer startServer(String id) {
        Optional<OpenttdServer> openttdServer = this.repository.getOpenttdServer(id);
        if (openttdServer.isPresent()) {
            OpenttdProcess openttdProcess = new OpenttdProcess(this.executor, this.eventBus);
            openttdProcess.setStartServerCommand(this.startServer);
            openttdProcess.setId(openttdServer.get().getId());
            openttdProcess.setPort(openttdServer.get().getPort());

            if (openttdServer.get().getSaveGame() != null) {
                openttdProcess.setSaveGame(openttdServer.get().getSaveGame().getPath());
            }

            handleCustomConfig(openttdServer.get(), openttdProcess);


            openttdProcess.start();
            processes.put(openttdProcess.getId(), openttdProcess);
            return this.getOpenttdServer(openttdServer.get().getId()).orElse(null);
        }
        throw new ServiceRuntimeException("Failed to start server. Server with name '" + id + "' does not exists!");
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

    public Optional<OpenttdServer> stop(String id) {
        if (this.processes.containsKey(id)) {
            this.processes.get(id).executeCommand(new QuitCommand(), true);
            this.processes.get(id).getProcessThread().stop();
            this.processes.remove(id);
            OpenttdServer patch = new OpenttdServer();
            patch.setInviteCode("");
            patch.setCurrentClients(0);
            patch.setMaxClients(0);
            patch.setCurrentCompanies(0);
            patch.setMaxCompanies(0);
            patch.setCurrentSpectators(0);

            this.updateServer(id, patch);
        }
        return enrich(this.repository.getOpenttdServer(id));

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
                this.processes.get(id).getProcessThread().write(cmd);
            }
        }
    }

    public void saveGame(String name) {
        this.createSaveGame(name, false, true);
    }

    public void autoSaveGame(String name) {
        this.createSaveGame(name, true, false);
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

    private void createSaveGame(String id, boolean autosave, boolean manually) {
        OpenttdServer openttdServer = this.repository.getOpenttdServer(id).orElse(null);
        if (this.processes.containsKey(id) && openttdServer != null) {

            String save = null;
            if (autosave) {
                save = getAutoSaveGameName(id).toFile().getAbsolutePath();
            }
            if (manually) {
                save = getSaveGameName(id).toFile().getAbsolutePath();
            }

            ServerFile serverFile = this.repository.serverFile(save + ".sav", ServerFileType.SAVE_GAME);
            String cmd = String.format("save \"%s\"", save);

            if (autosave) {
                openttdServer.setSaveGame(serverFile);
            }
            if (manually) {
                openttdServer.setSaveGame(serverFile);
            }


            this.repository.updateServer(id, openttdServer);
            this.processes.get(id).getProcessThread().write(cmd);
            for (int i = 0; i < 100; i++) {
                ServerFile updated = this.repository.serverFile(save + ".sav", ServerFileType.SAVE_GAME);
                if (updated.getLastModified() > serverFile.getLastModified()) {
                    return;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            throw new ServiceRuntimeException("Save may have failed. Check logs!");
        } else {
            throw new ServiceRuntimeException("Can't create save game for server '" + id + "'. Server does not exist or is not running!");
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
