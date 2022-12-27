package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.ServerInfoCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.OpenttdServer;
import de.litexo.services.OpenttdService;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class UpdateServerInfo {

    @Inject
    OpenttdService service;

    @Inject
    EventBus eventBus;

    @Inject
    ManagedExecutor executor;


    @PostConstruct
    void init() {
        System.out.println("INIT UpdateServerInfo Scheduler");
        this.eventBus.observe(OpenttdTerminalUpdateEvent.class, this.getClass(), openttdTerminalUpdateEvent -> {
            // Important execute this async because the subscriber is blocking the emitter of the event
            executor.execute(() -> handleTerminalUpdateEvent(openttdTerminalUpdateEvent));
        });
    }

    @Scheduled(every = "120s")
    void checkAutoPauseUnpause() {
        for (OpenttdProcess process : service.getProcesses()) {
            updateServerInfo(process);
        }
    }

    void updateServerInfo(OpenttdProcess process) {
        OpenttdServer openttdServer = service.getOpenttdServer(process.getId()).orElse(null);
        if (openttdServer != null) {
            ServerInfoCommand cmd = process.executeCommand(new ServerInfoCommand(), false);
            if (cmd.isExecuted()) {
                openttdServer.setInviteCode(cmd.getInviteCode());
                openttdServer.setCurrentClients(cmd.getCurrentClients());
                openttdServer.setMaxClients(cmd.getMaxClients());
                openttdServer.setCurrentCompanies(cmd.getCurrentCompanies());
                openttdServer.setMaxCompanies(cmd.getMaxCompanies());
                openttdServer.setCurrentSpectators(cmd.getCurrentSpectators());
                this.service.updateServer(openttdServer.getId(), openttdServer);
            }
        }
    }

    void handleTerminalUpdateEvent(OpenttdTerminalUpdateEvent openttdTerminalUpdateEvent) {
        if (
                openttdTerminalUpdateEvent.getText().contains("has started a new company")
                        || openttdTerminalUpdateEvent.getText().contains("has joined company")
                        || openttdTerminalUpdateEvent.getText().contains("has joined the game")
                        || openttdTerminalUpdateEvent.getText().contains("has left the game")
                        || openttdTerminalUpdateEvent.getText().contains("closed connection")
                        || openttdTerminalUpdateEvent.getText().contains("has joined spectators")
        ) {
            Optional<OpenttdProcess> process = this.service.getProcesses().stream().filter(p -> p.getProcessThread().getUuid().equals(openttdTerminalUpdateEvent.getProcessId())).findAny();
            if (process.isPresent()) {
                updateServerInfo(process.get());
            }
        }
    }
}
