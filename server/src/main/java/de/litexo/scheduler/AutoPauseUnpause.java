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
public class AutoPauseUnpause {

    @Inject
    OpenttdService service;

    @Inject
    EventBus eventBus;

    @Inject
    ManagedExecutor executor;

    @PostConstruct
    void init() {
        System.out.println("INIT AutoPauseUnpause Scheduler");
        this.eventBus.observe(OpenttdTerminalUpdateEvent.class, this.getClass(), openttdTerminalUpdateEvent -> {
            // Important execute this async because the subscriber is blocking the emitter of the event
            executor.execute(() -> handleTerminalUpdateEvent(openttdTerminalUpdateEvent));
        });
    }

    @Scheduled(every = "600s")
    void checkAutoPauseUnpause() {
        for (OpenttdProcess process : service.getProcesses()) {
            doPauseOrUnpause(process);
        }
    }

    void doPauseOrUnpause(OpenttdProcess process) {
        Optional<OpenttdServer> openttdServer = service.getOpenttdServer(process.getId());
        if (openttdServer.isPresent() && !openttdServer.get().isAutoPause()) {
            System.out.println("Autopause is disabled for Server: " + openttdServer.get().getName());
            return;
        }

        ServerInfoCommand cmd = process.executeCommand(new ServerInfoCommand(), false);
        System.out.println("Server-Info: " + process.getId() + " " + cmd);
        if (cmd.isExecuted()) {
            if (cmd.getCurrentClients() == cmd.getCurrentSpectators()) {
                System.out.println("Pause Server: " + process.getId());
                PauseCommand pauseCommand = process.executeCommand(new PauseCommand(), false);
                if (pauseCommand.isExecuted()) {
                    openttdServer.get().setPaused(true);
                }
            } else {
                System.out.println("Unpause Server: " + process.getId());
                UnpauseCommand unpauseCommand = process.executeCommand(new UnpauseCommand(), false);
                if (unpauseCommand.isExecuted()) {
                    openttdServer.get().setPaused(false);
                }
            }
        }
    }

    void handleTerminalUpdateEvent(OpenttdTerminalUpdateEvent openttdTerminalUpdateEvent) {
        System.out.println("XXXHandle Terminal Update:" + openttdTerminalUpdateEvent.getText());
        if (
                openttdTerminalUpdateEvent.getText().contains("has started a new company")
                        || openttdTerminalUpdateEvent.getText().contains("has joined company")
                        || openttdTerminalUpdateEvent.getText().contains("has left the game")
                        || openttdTerminalUpdateEvent.getText().contains("closed connection")
                        || openttdTerminalUpdateEvent.getText().contains("has joined spectators")
        ) {
            Optional<OpenttdProcess> process = this.service.getProcesses().stream().filter(p -> p.getProcessThread().getUuid().equals(openttdTerminalUpdateEvent.getProcessId())).findAny();
            if (process.isPresent()) {
                doPauseOrUnpause(process.get());
            }
        }
    }
}
