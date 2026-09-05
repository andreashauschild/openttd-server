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
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class AutoPauseUnpause {

    private static final Logger LOG = Logger.getLogger(AutoPauseUnpause.class);

    @Inject
    OpenttdService service;

    @Inject
    DefaultRepository repository;

    @Inject
    EventBus eventBus;

    @Inject
    ManagedExecutor executor;

    @Inject
    UpdateServerInfo updateServerInfo;

    /**
     * One flag per process, so that a burst of join and leave lines does not start a check per line.
     */
    private final Map<String, AtomicBoolean> pendingChecks = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        LOG.info("INIT AutoPauseUnpause Scheduler");
        this.eventBus.observe(OpenttdTerminalUpdateEvent.class, this.getClass(), openttdTerminalUpdateEvent -> {
            // Important execute this async because the subscriber is blocking the emitter of the event
            executor.execute(() -> handleTerminalUpdateEvent(openttdTerminalUpdateEvent));
        });
    }

    @Scheduled(every = "30s", concurrentExecution = ConcurrentExecution.SKIP)
    void checkAutoPauseUnpause() {
        for (OpenttdProcess process : service.getProcesses()) {
            doPauseOrUnpause(process);
        }
    }

    void doPauseOrUnpause(OpenttdProcess process) {
        if (!process.isAlive()) {
            LOG.debugf("Pause/unpause check of server '%s' is skipped, its process is not running", process.getId());
            return;
        }
        OpenttdServer openttdServer = service.getOpenttdServer(process.getId()).orElse(null);

        if(openttdServer!=null){
            if (!openttdServer.isAutoPause()) {
                LOG.debugf("Autopause is disabled for Server: %s", openttdServer.getName());
                return;
            }

            ServerInfoCommand cmd = process.executeCommand(new ServerInfoCommand(), false);
            if (cmd.isExecuted()) {
                if (cmd.getCurrentClients() == cmd.getCurrentSpectators()) {
                    LOG.debugf("Pause Server: %s", process.getId());
                    PauseCommand pauseCommand = process.executeCommand(new PauseCommand(this.repository), false);
                    if (pauseCommand.isExecuted()) {
                        openttdServer.setPaused(true);
                    }
                } else {
                    LOG.debugf("Unpause Server: %s", process.getId());
                    UnpauseCommand unpauseCommand = process.executeCommand(new UnpauseCommand(this.repository), false);
                    if (unpauseCommand.isExecuted()) {
                        openttdServer.setPaused(false);
                    }
                }
                // Reuse the result instead of sending a second 'server_info' from UpdateServerInfo.
                this.updateServerInfo.applyServerInfo(openttdServer, cmd);
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
            Optional<OpenttdProcess> process = this.service.findProcessByThreadUuid(openttdTerminalUpdateEvent.getProcessId());
            if (process.isPresent()) {
                AtomicBoolean pending = this.pendingChecks.computeIfAbsent(openttdTerminalUpdateEvent.getProcessId(), key -> new AtomicBoolean(false));
                if (!pending.compareAndSet(false, true)) {
                    LOG.debugf("A pause/unpause check for process '%s' is already running", openttdTerminalUpdateEvent.getProcessId());
                    return;
                }
                try {
                    doPauseOrUnpause(process.get());
                } finally {
                    pending.set(false);
                }
            }
        }
    }
}
