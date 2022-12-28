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

    protected Command(String command) {
        this.command = command;
        this.marker = "@@@@_" + getType() + "_" + System.currentTimeMillis() + "_@@@@";
    }

    public String getType() {
        return getClass().getSimpleName();
    }


    public abstract boolean check(String logs);

    /**
     * Will be called if the command was successfully executed
     *
     * @param openttdServeId id of server that executed the command
     */
    public void onSuccess(String openttdServeId){

    }

    public Command execute(ProcessThread process, String openttdServerId ) {
        process.write(this.marker);
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(200L);
                if (!cmdSend) {
                    process.write(this.command);
                    this.cmdSend = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            int beginIndex = process.getLogs().indexOf(this.marker);
            if (beginIndex == -1) {
                continue;
            }
            this.rawResult = process.getLogs().substring(beginIndex);
            if (check(this.rawResult)) {
                this.executed = true;
                this.onSuccess(openttdServerId);
                return this;
            }
        }
        return this;
    }

}
