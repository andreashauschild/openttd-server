package de.litexo.scheduler;


import de.litexo.OpenttdProcess;
import de.litexo.commands.PauseCommand;
import de.litexo.commands.ServerInfoCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.EventBus;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.model.external.OpenttdServer;
import de.litexo.repository.DefaultRepository;
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
    DefaultRepository repository;

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

    @Scheduled(every = "30s")
    void checkAutoPauseUnpause() {
        for (OpenttdProcess process : service.getProcesses()) {
            doPauseOrUnpause(process);
        }
    }

    void doPauseOrUnpause(OpenttdProcess process) {
        OpenttdServer openttdServer = service.getOpenttdServer(process.getId()).orElse(null);

        if(openttdServer!=null){
            if (!openttdServer.isAutoPause()) {
                System.out.println("Autopause is disabled for Server: " + openttdServer.getName());
                return;
            }

            ServerInfoCommand cmd = process.executeCommand(new ServerInfoCommand(), false);
            if (cmd.isExecuted()) {
                if (cmd.getCurrentClients() == cmd.getCurrentSpectators()) {
                    System.out.println("Pause Server: " + process.getId());
                    PauseCommand pauseCommand = process.executeCommand(new PauseCommand(this.repository), false);
                    if (pauseCommand.isExecuted()) {
                        openttdServer.setPaused(true);
                    }
                } else {
                    System.out.println("Unpause Server: " + process.getId());
                    UnpauseCommand unpauseCommand = process.executeCommand(new UnpauseCommand(this.repository), false);
                    if (unpauseCommand.isExecuted()) {
                        openttdServer.setPaused(false);
                    }
                }
            }
        }

    }

    void handleTerminalUpdateEvent(OpenttdTerminalUpdateEvent openttdTerminalUpdateEvent) {
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
