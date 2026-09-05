package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.commands.ServerInfoCommand;
import de.litexo.model.external.OpenttdServer;
import de.litexo.services.OpenttdService;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class UpdateServerInfo {

    private static final Logger LOG = Logger.getLogger(UpdateServerInfo.class);

    @Inject
    OpenttdService service;

    /**
     * Periodic refresh only. The event driven refresh used to live here as well, but it reacted on the same join and
     * leave lines as {@link AutoPauseUnpause}, so two 'server_info' commands were sent to one console at the same time
     * and their output interleaved. {@link AutoPauseUnpause} now hands its result over via {@link #applyServerInfo}.
     */
    @Scheduled(every = "120s", concurrentExecution = ConcurrentExecution.SKIP)
    void refreshServerInfo() {
        for (OpenttdProcess process : service.getProcesses()) {
            if (!process.isAlive()) {
                LOG.debugf("Server info of server '%s' is not refreshed, its process is not running", process.getId());
                continue;
            }
            updateServerInfo(process);
        }
    }

    void updateServerInfo(OpenttdProcess process) {
        OpenttdServer openttdServer = service.getOpenttdServer(process.getId()).orElse(null);
        if (openttdServer != null) {
            ServerInfoCommand cmd = process.executeCommand(new ServerInfoCommand(), false);
            applyServerInfo(openttdServer, cmd);
        }
    }

    /**
     * Copies the result of an already executed 'server_info' onto the server and persists it.
     */
    void applyServerInfo(OpenttdServer openttdServer, ServerInfoCommand cmd) {
        if (openttdServer == null || cmd == null || !cmd.isExecuted()) {
            return;
        }
        openttdServer.setInviteCode(cmd.getInviteCode());
        openttdServer.setCurrentClients(cmd.getCurrentClients());
        openttdServer.setMaxClients(cmd.getMaxClients());
        openttdServer.setCurrentCompanies(cmd.getCurrentCompanies());
        openttdServer.setMaxCompanies(cmd.getMaxCompanies());
        openttdServer.setCurrentSpectators(cmd.getCurrentSpectators());
        this.service.updateServer(openttdServer.getId(), openttdServer);
        LOG.debugf("Server info of server '%s' updated", openttdServer.getId());
    }
}
