package de.litexo.commands;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.litexo.ProcessThread;
import de.litexo.api.ServiceRuntimeException;
import lombok.Getter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PauseCommand.class, name = "PauseCommand"),
        @JsonSubTypes.Type(value = UnpauseCommand.class, name = "UnpauseCommand"),
        @JsonSubTypes.Type(value = ServerInfoCommand.class, name = "ServerInfoCommand"),
        @JsonSubTypes.Type(value = ClientsCommand.class, name = "ClientsCommand")
})
public abstract class Command {

    String marker;
    String command;

    String rawResult;

    @Getter
    boolean executed = false;

    boolean cmdSend = false;

    public Command(String command) {
        this.command = command;
        this.marker = "@@@@_" + getType() + "_" + System.currentTimeMillis() + "_@@@@";
    }

    public String getType() {
        return getClass().getSimpleName();
    }


    public abstract boolean check(String logs);

    public Command execute(ProcessThread process) {
        process.write(this.marker);
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(200L);
                if (!cmdSend) {
                    process.write(this.command);
                    this.cmdSend = true;
                }
            } catch (InterruptedException e) {
                throw new ServiceRuntimeException(e);
            }
            int beginIndex = process.getLogs().indexOf(this.marker);
            if (beginIndex == -1) {
                continue;
            }
            this.rawResult = process.getLogs().substring(beginIndex);
            if (check(this.rawResult)) {
                this.executed = true;
                return this;
            }
        }
        return this;
    }

}
