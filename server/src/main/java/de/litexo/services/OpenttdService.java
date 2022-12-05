package de.litexo.services;

import de.litexo.OpenttdProcess;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.commands.Command;
import de.litexo.events.EventBus;
import de.litexo.model.OpenttdServer;
import de.litexo.model.OpenttdServerConfig;
import de.litexo.model.ServerFile;
import de.litexo.model.ServerFileType;
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
import java.util.*;

@ApplicationScoped
public class OpenttdService {
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

    public void dumpProcessData(String name, String dir) {
        if (this.processes.containsKey(name)) {
            try {
                FileUtils.write(Paths.get(dir).resolve(name + "-" + System.currentTimeMillis() + "-dump.txt").toFile(), this.processes.get(name).getProcessThread().getLogs(), StandardCharsets.UTF_8, false);
            } catch (IOException e) {
                new ServiceRuntimeException(e);
            }
        } else {
            new ServiceRuntimeException("Process with name '" + name + "' is not running order does not exists");
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
        openttdProcess.setName(name);
        openttdProcess.setPort(port);
        openttdProcess.setSaveGame(savegame);
        openttdProcess.setConfig(config);
        openttdProcess.start();
        processes.put(openttdProcess.getName(), openttdProcess);
        return openttdProcess;
    }

    public OpenttdServer startServer(String name, String savegame) {
        Optional<OpenttdServer> openttdServer = this.repository.getOpenttdServer(name);
        if (openttdServer.isPresent()) {
            OpenttdProcess openttdProcess = new OpenttdProcess(this.executor, this.eventBus);
            openttdProcess.setStartServerCommand(this.startServer);
            openttdProcess.setName(openttdServer.get().getName());
            openttdProcess.setPort(openttdServer.get().getPort());

            if (savegame != null) {
                openttdProcess.setSaveGame(savegame);
            } else if (openttdServer.get().getSaveGame() != null) {
                openttdProcess.setSaveGame(openttdServer.get().getSaveGame().getPath());
            } else if (openttdServer.get().getStartSaveGame() != null) {
                openttdProcess.setSaveGame(openttdServer.get().getStartSaveGame().getPath());
            }

            if (openttdServer.get().getConfig() != null) {
                openttdProcess.setConfig(openttdServer.get().getConfig().getPath());
            }

            openttdProcess.start();
            processes.put(openttdProcess.getName(), openttdProcess);
            return this.getOpenttdServer(openttdServer.get().getName()).get();
        }
        throw new ServiceRuntimeException("Failed to start server. Server with name '" + name + "' does not exists!");
    }


    public void stop(String name) {
        if (this.processes.containsKey(name)) {
            this.processes.get(name).getProcessThread().stop();
            this.processes.remove(name);
        }
    }


    public void sendTerminalCommand(String name, String cmd) {
        if (this.processes.containsKey(name)) {
            this.processes.get(name).getProcessThread().write(cmd);
        }
    }

    public void saveGame(String name) {
        this.createSaveGame(name, false, true);
    }

    public void autoSaveGame(String name) {
        this.createSaveGame(name, true, false);
    }


    private Path getSaveGameName(String name) {
        return this.repository.getOpenttdSaveDirPath().resolve(name + "_manually_save");
    }

    private Path getAutoSaveGameName(String name) {
        return this.repository.getOpenttdSaveDirPath().resolve(name + "_auto_save");
    }

    public OpenttdServerConfig getOpenttdServerConfig() {
        OpenttdServerConfig openttdServerData = this.repository.getOpenttdServerConfig();
        openttdServerData.getServers().forEach(s -> enrich(s));
        return openttdServerData;
    }

    public Optional<OpenttdServer> getOpenttdServer(String name) {
        return enrich(this.repository.getOpenttdServer(name));
    }

    public OpenttdServer addServer(OpenttdServer server) {
        return enrich(this.repository.addServer(server));
    }

    public OpenttdServer updateServer(OpenttdServer server) {
        return enrich(this.repository.updateServer(server));
    }

    public void deleteServer(String name) {
        this.repository.deleteServer(name);
        stop(name);
    }

    private OpenttdServer enrich(OpenttdServer server) {
        if (server != null) {
            if (this.processes.containsKey(server.getName())) {
                server.setProcess(this.processes.get(server.getName()));
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

    private void createSaveGame(String name, boolean autosave, boolean manually) {
        if (this.processes.containsKey(name)) {
            OpenttdServer openttdServer = this.repository.getOpenttdServer(name).get();
            String save = null;
            if (autosave) {
                save = getAutoSaveGameName(name).toFile().getAbsolutePath();
            }
            if (manually) {
                save = getSaveGameName(name).toFile().getAbsolutePath();
            }

            ServerFile serverFile = this.repository.serverFile(save + ".sav", ServerFileType.SAVE_GAME);
            String cmd = String.format("save \"%s\"", save);

            if (autosave) {
                openttdServer.setAutoSaveGame(serverFile);
            }
            if (manually) {
                openttdServer.setSaveGame(serverFile);
            }


            this.repository.updateServer(openttdServer);
            this.processes.get(name).getProcessThread().write(cmd);
            for (int i = 0; i < 100; i++) {
                ServerFile updated = this.repository.serverFile(save + ".sav", ServerFileType.SAVE_GAME);
                if (updated.getLastModified() > serverFile.getLastModified()) {
                    return;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            throw new ServiceRuntimeException("Save may have failed. Check logs!");
        } else {
            throw new ServiceRuntimeException("Can't create save game for server '" + name + "'. Server does not exist or is not running!");
        }
    }
}
