package de.litexo.services;

import de.litexo.OpenttdProcess;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.commands.Command;
import de.litexo.events.EventBus;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.external.ServerFile;
import de.litexo.model.external.ServerFileType;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

@ApplicationScoped
public class OpenttdService {
    public static final String MANUALLY_SAVE_INFIX = "_manually_save_";
    public static final String AUTO_SAVE_INFIX = "_auto_save_";
    @ConfigProperty(name = "start-server.command")
    String startServerCommand;

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
                FileUtils.write(Paths.get(dir).resolve(id + "-" + System.currentTimeMillis() + "-dump.txt").toFile(), this.processes.get(id).getProcessThread().getLogs(), StandardCharsets.UTF_8, false);
            } catch (IOException e) {
                new ServiceRuntimeException(e);
            }
        } else {
            new ServiceRuntimeException("Process with name '" + id + "' is not running order does not exists");
        }
    }

    public Command execCommand(String processName, Command command) {
        if (this.processes.containsKey(processName)) {
            return command.execute(this.processes.get(processName).getProcessThread());
        } else {
            new ServiceRuntimeException("Process with name '" + processName + "' is not running order does not exists");
        }
        return null;
    }


    public OpenttdProcess start(String name,
                                Integer port,
                                String savegame,
                                String config) {
        OpenttdProcess openttdProcess = new OpenttdProcess(this.executor, this.eventBus);
        openttdProcess.setStartServerCommand(this.startServer);
        openttdProcess.setId(name);
        openttdProcess.setPort(port);
        openttdProcess.setSaveGame(savegame);
        openttdProcess.setConfig(config);
        openttdProcess.start();
        processes.put(openttdProcess.getId(), openttdProcess);
        return openttdProcess;
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

            if (openttdServer.get().getConfig() != null) {
                openttdProcess.setConfig(openttdServer.get().getConfig().getPath());
            }

            openttdProcess.start();
            processes.put(openttdProcess.getId(), openttdProcess);
            return this.getOpenttdServer(openttdServer.get().getId()).get();
        }
        throw new ServiceRuntimeException("Failed to start server. Server with name '" + id + "' does not exists!");
    }


    public void stop(String id) {
        if (this.processes.containsKey(id)) {
            this.processes.get(id).getProcessThread().stop();
            this.processes.remove(id);
        }
    }

    public void setTerminalOpenInUi(String id) {
        if (this.processes.containsKey(id)) {
            this.processes.get(id).setLastUiTerminalActivity(System.currentTimeMillis());
        }
    }


    public void sendTerminalCommand(String id, String cmd) {
        if (this.processes.containsKey(id)) {
            this.processes.get(id).getProcessThread().write(cmd);
        }
    }

    public void saveGame(String name) {
        this.createSaveGame(name, false, true);
    }

    public void autoSaveGame(String name) {
        this.createSaveGame(name, true, false);
    }


    private Path getSaveGameName(String id) {
        return this.repository.getOpenttdSaveDirPath().resolve(id + MANUALLY_SAVE_INFIX +System.currentTimeMillis());
    }

    private Path getAutoSaveGameName(String id) {
        return this.repository.getOpenttdSaveDirPath().resolve(id + AUTO_SAVE_INFIX +System.currentTimeMillis());
    }

    public InternalOpenttdServerConfig getOpenttdServerConfig() {
        InternalOpenttdServerConfig openttdServerData = this.repository.getOpenttdServerConfig();
        openttdServerData.getServers().forEach(s -> enrich(s));
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
        if (this.processes.containsKey(id)) {
            OpenttdServer openttdServer = this.repository.getOpenttdServer(id).get();
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
                        throw new ServiceRuntimeException(e);
                    }
                }
            }
            throw new ServiceRuntimeException("Save may have failed. Check logs!");
        } else {
            throw new ServiceRuntimeException("Can't create save game for server '" + id + "'. Server does not exist or is not running!");
        }
    }


}
