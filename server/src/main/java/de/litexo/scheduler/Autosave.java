package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.services.OpenttdService;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class Autosave {

    @Inject
    OpenttdService service;

    @Scheduled(every = "600s")
    void checkAutosave() {
        InternalOpenttdServerConfig serverConfig = this.service.getOpenttdServerConfig();
        for (OpenttdProcess process : service.getProcesses()) {
            Optional<OpenttdServer> openttdServer = service.getOpenttdServer(process.getId());
            if (openttdServer.isPresent()) {
                if (openttdServer.get().getSaveGame() == null || !openttdServer.get().getSaveGame().isExists()) {
                    this.service.autoSaveGame(openttdServer.get().getId());
                } else {
                    long ageInSeconds = (System.currentTimeMillis() - openttdServer.get().getSaveGame().getLastModified()) / 1000;
                    long outDated = serverConfig.getAutoSaveMinutes() * 60;
                    if (ageInSeconds > outDated) {
                        System.out.println("Last save game was made before '" + ageInSeconds + "' seconds. autosave is executed");
                        this.service.autoSaveGame(openttdServer.get().getId());
                    } else {
                        System.out.println("Last save game was made '" + ageInSeconds + "' no autosave was executed");
                    }
                }
            }
        }
    }
}
