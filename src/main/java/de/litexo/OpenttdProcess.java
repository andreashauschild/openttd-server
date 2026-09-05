package de.litexo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.litexo.api.ServiceRuntimeException;
import de.litexo.commands.Command;
import de.litexo.events.EventBus;
import de.litexo.model.external.BaseProcess;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Data
@Accessors(chain = true)
public class OpenttdProcess {

    private static final Logger LOG = Logger.getLogger(OpenttdProcess.class);

    private static final long UI_TERMINAL_ACTIVITY_DISABLE_COMMANDS_THRESHOLD = 30000;

    private static final long COMMAND_LOCK_TIMEOUT_MS = 30_000;

    private static final long STOP_LOCK_TIMEOUT_MS = 65_000;

    private List<String> startServerCommand = new ArrayList<>();

    /**
     * This must always be the id of the OpenTTDServer that is executed by this process
     */
    private String id = null;

    private Integer port = null;

    private String saveGame = null;

    private String config = null;

    /**
     * This field is used to check if the terminal is open in the server ui. The client poll and endpoint that updates that value. No auto commands, like
     * pause, unpause or server info will be executed in that time. That should prevent that the terminal is polluted with auto commands
     */
    @JsonIgnore
    private Long lastUiTerminalActivity;

    @JsonIgnore
    private ProcessThread processThread;

    @JsonIgnore
    private ExecutorService executorService;

    @JsonIgnore
    private EventBus eventBus;

    /**
     * There is only one console per process. Two commands running at the same time interleave their output, which made
     * the marker based result detection unreliable, so every access to the console is serialized through this lock.
     */
    @JsonIgnore
    private final ReentrantLock commandLock = new ReentrantLock(true);

    /**
     * Counts the automatic commands that were not sent because the terminal is open in the ui. The commands
     * themselves are deliberately not written into the console history, that is the pollution the feature avoids, so
     * this counter is the only trace of them. Exposed on {@link BaseProcess#getSkippedCommands()}.
     */
    @JsonIgnore
    private final AtomicInteger skippedCommands = new AtomicInteger();

    /**
     * @return true if the process of this server is still up. A dead process cannot answer a command, so the
     *         schedulers skip it instead of waiting for the timeout of every command they would send.
     */
    @JsonIgnore
    public boolean isAlive() {
        return this.processThread != null && this.processThread.isAlive();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BaseProcess getProcess() {
        return processThread.toModel().setSkippedCommands(this.skippedCommands.get());
    }

    public OpenttdProcess() {
    }

    public OpenttdProcess(ExecutorService executorService, EventBus eventBus) {
        this.executorService = executorService;
        this.eventBus = eventBus;
    }

    public void start() {
        try {


            List<String> cmd = new ArrayList<>();

            if (this.id == null) {
                this.id = "Server-" + System.currentTimeMillis();
            }

            if (this.startServerCommand.isEmpty()) {
                cmd.add("openttd");
                cmd.add("-D");
            } else {
                cmd.addAll(this.startServerCommand);
            }


            if (this.port != null) {
                cmd.add("0.0.0.0:" + this.port);
            }

            if (isNotEmpty(this.saveGame)) {
                cmd.add("-g");
                cmd.add(this.saveGame);
            }

            if (isNotEmpty(this.config)) {
                cmd.add("-c");
                cmd.add(this.config);
            }
            this.processThread = new ProcessThread(this.executorService, cmd, this.eventBus);
            this.executorService.execute(processThread);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error: Failed to start process", e);
        }
    }

    public <T extends Command> T executeCommand(T command, boolean executeWithActiveClientTerminal) {
        if (this.processThread == null) {
            return command;
        }
        if (!executeWithActiveClientTerminal && isUiTerminalOpenedByClient()) {
            LOG.debugf("Command '%s' will be skipped because there is a open ui terminal", command.getType());
            this.skippedCommands.incrementAndGet();
            return command;
        }
        lockConsole(COMMAND_LOCK_TIMEOUT_MS);
        try {
            return (T) command.execute(this.processThread, this.id);
        } finally {
            this.commandLock.unlock();
        }
    }

    /**
     * Writes raw user input to the console. Only the write is serialized, there is no result to wait for.
     */
    public void writeToConsole(String command) {
        if (this.processThread == null || command == null) {
            return;
        }
        lockConsole(COMMAND_LOCK_TIMEOUT_MS);
        try {
            this.processThread.write(command);
        } finally {
            this.commandLock.unlock();
        }
    }

    /**
     * Asks the process to quit and waits for it. A running command (a save game can take a while) is allowed to finish
     * first, so that the save game is complete before OpenTTD goes down.
     *
     * @param quitTimeoutMs time the process gets to exit on its own before it is killed
     * @return true if the process exited on its own
     */
    public boolean stop(long quitTimeoutMs) {
        if (this.processThread == null) {
            return true;
        }
        boolean locked = tryLockConsole(STOP_LOCK_TIMEOUT_MS);
        if (!locked) {
            LOG.warnf("Console of server '%s' is still busy. The process will be stopped anyway.", this.id);
        }
        try {
            return this.processThread.stopGracefully(quitTimeoutMs);
        } finally {
            if (locked) {
                this.commandLock.unlock();
            }
        }
    }

    private void lockConsole(long timeoutMs) {
        if (!tryLockConsole(timeoutMs)) {
            throw new ServiceRuntimeException("Timeout waiting for console lock of server " + this.id);
        }
    }

    private boolean tryLockConsole(long timeoutMs) {
        try {
            return this.commandLock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceRuntimeException("Interrupted while waiting for the console lock of server " + this.id, e);
        }
    }

    private boolean isUiTerminalOpenedByClient() {
        if (this.lastUiTerminalActivity == null) {
            return false;
        }
        return (System.currentTimeMillis() - this.lastUiTerminalActivity) < UI_TERMINAL_ACTIVITY_DISABLE_COMMANDS_THRESHOLD;
    }


}
