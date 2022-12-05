package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.model.OpenttdServer;
import de.litexo.model.OpenttdServerConfig;
import de.litexo.services.OpenttdService;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class Autosave {

    @Inject
    OpenttdService service;

    @Scheduled(every = "10s")
    void checkAutosave() {
        OpenttdServerConfig serverConfig = this.service.getOpenttdServerConfig();
        for (OpenttdProcess process : service.getProcesses()) {
            Optional<OpenttdServer> openttdServer = service.getOpenttdServer(process.getName());
            if (openttdServer.isPresent()) {
                if (openttdServer.get().getAutoSaveGame() == null || !openttdServer.get().getAutoSaveGame().isExists()) {
                    this.service.autoSaveGame(openttdServer.get().getName());
                } else {
                    long ageInSeconds = (System.currentTimeMillis() - openttdServer.get().getAutoSaveGame().getLastModified()) / 1000;
                    long outDated = serverConfig.getAutoSaveMinutes() * 60;
                    if (ageInSeconds > outDated) {
                        System.out.println("Last save game was made before '" + ageInSeconds + "' seconds. autosave is executed");
                        this.service.autoSaveGame(openttdServer.get().getName());
                    } else {
                        System.out.println("Last save game was made '" + ageInSeconds + "' no autosave was executed");
                    }
                }
            }
        }
    }
}
