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

    @Scheduled(every = "10s")
    void checkAutosave() {
        InternalOpenttdServerConfig serverConfig = this.service.getOpenttdServerConfig();
        for (OpenttdProcess process : service.getProcesses()) {
            Optional<OpenttdServer> openttdServer = service.getOpenttdServer(process.getId());
            if (openttdServer.isPresent()) {
                if (openttdServer.get().getSaveGame() == null || !openttdServer.get().getSaveGame().isExists()) {
                    save(openttdServer.get());
                } else {
                    long ageInSeconds = (System.currentTimeMillis() - openttdServer.get().getSaveGame().getLastModified()) / 1000;
                    long outDated = serverConfig.getAutoSaveMinutes() * 60;
                    if (ageInSeconds > outDated) {
                        System.out.println("Last save game was made before '" + ageInSeconds + "' seconds. autosave is executed");
                        save(openttdServer.get());
                    } else {
                        System.out.println("Last save game was made '" + ageInSeconds + "' no autosave was executed");
                    }
                }
            }
        }
    }

    private void save(OpenttdServer server){
        if (!server.isAutoSave()) {
            System.out.println("Autosave is disabled for Server: " + server.getName());
        }else{
            this.service.autoSaveGame(server.getId());
        }

    }
}
