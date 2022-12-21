package de.litexo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.litexo.commands.Command;
import de.litexo.events.EventBus;
import de.litexo.model.external.BaseProcess;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Data
@Accessors(chain = true)
public class OpenttdProcess {

    private static final long UI_TERMINAL_ACTIVITY_DISABLE_COMMANDS_THRESHOLD = 30000;

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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BaseProcess getProcess() {
        return processThread.toModel();
    }

    public OpenttdProcess() {
    }

    public OpenttdProcess(ExecutorService executorService, EventBus eventBus) {
        this.executorService = executorService;
        this.eventBus = eventBus;
    }

    public void start() {
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
    }

    public <T extends Command> T executeCommand(T command, boolean executeWithActiveClientTerminal) {
        if (this.processThread != null) {
            if (executeWithActiveClientTerminal) {
                return (T) command.execute(this.processThread);
            } else if (!isUiTerminalOpenedByClient()) {
                return (T) command.execute(this.processThread);
            } else {
                System.out.println("Command '" + command.getType() + "' will be skipped because there is a open ui terminal");
            }
        }
        return command;
    }

    private boolean isUiTerminalOpenedByClient() {
        if (this.lastUiTerminalActivity == null) {
            return false;
        }
        return (System.currentTimeMillis() - this.lastUiTerminalActivity) > UI_TERMINAL_ACTIVITY_DISABLE_COMMANDS_THRESHOLD;
    }


}
