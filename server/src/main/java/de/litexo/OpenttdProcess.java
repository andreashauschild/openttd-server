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

    private List<String> startServerCommand = new ArrayList<>();

    private String name = null;

    private Integer port = null;

    private String saveGame = null;

    private String config = null;

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

        if (this.name == null) {
            this.name = "Server-" + System.currentTimeMillis();
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

    public <T extends Command> T executeCommand(T command) {
        if (this.processThread != null) {
            return (T) command.execute(this.processThread);
        }
        return command;
    }


}
