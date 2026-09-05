package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.model.external.OpenttdServer;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.services.OpenttdService;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class Autosave {

    private static final Logger LOG = Logger.getLogger(Autosave.class);

    @Inject
    OpenttdService service;

    // A save can block for up to a minute, so a tick must never pile up on the previous one.
    @Scheduled(every = "10s", concurrentExecution = ConcurrentExecution.SKIP)
    void checkAutosave() {
        InternalOpenttdServerConfig serverConfig = this.service.getOpenttdServerConfig();
        if (serverConfig.getAutoSaveMinutes() <= 0) {
            LOG.debugf("Auto save is skipped because save intervall is <= 0 -> %d", serverConfig.getAutoSaveMinutes());
            return;
        }
        for (OpenttdProcess process : service.getProcesses()) {
            if (!process.isAlive()) {
                LOG.debugf("Autosave of server '%s' is skipped, its process is not running", process.getId());
                continue;
            }
            try {
                autosaveIfOutdated(process, serverConfig);
            } catch (Exception e) {
                // A server that cannot be saved must not stop the autosave of the other servers. A process that died
                // while the save was running is an expected state here, so the message is enough and a stack trace
                // would only bury the real errors.
                LOG.warnf("Autosave of server '%s' failed: %s", process.getId(), e.getMessage());
            }
        }
    }

    private void autosaveIfOutdated(OpenttdProcess process, InternalOpenttdServerConfig serverConfig) {
        Optional<OpenttdServer> openttdServer = service.getOpenttdServer(process.getId());
        if (openttdServer.isPresent()) {
            if (openttdServer.get().getSaveGame() == null || !openttdServer.get().getSaveGame().isExists()) {
                save(openttdServer.get());
            } else {
                long ageInSeconds = (System.currentTimeMillis() - openttdServer.get().getSaveGame().getLastModified()) / 1000;
                long outDated = serverConfig.getAutoSaveMinutes() * 60;
                if (ageInSeconds > outDated) {
                    LOG.infof("Last save game was made before '%d' seconds. autosave is executed", ageInSeconds);
                    save(openttdServer.get());
                } else {
                    LOG.debugf("Last save game was made '%d' seconds ago, no autosave was executed", ageInSeconds);
                }
            }
        }
    }

    private void save(OpenttdServer server) {
        if (!server.isAutoSave()) {
            LOG.debugf("Autosave is disabled for Server: %s", server.getName());
        } else {
            this.service.autoSaveGame(server.getId());
        }

    }
}
