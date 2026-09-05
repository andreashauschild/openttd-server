package de.litexo.commands;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.litexo.ProcessThread;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PauseCommand.class, name = "PauseCommand"),
        @JsonSubTypes.Type(value = UnpauseCommand.class, name = "UnpauseCommand"),
        @JsonSubTypes.Type(value = ServerInfoCommand.class, name = "ServerInfoCommand"),
        @JsonSubTypes.Type(value = ClientsCommand.class, name = "ClientsCommand")
})
public abstract class Command {

    private static final AtomicLong MARKER_SEQUENCE = new AtomicLong();

    /**
     * Failure reason of a command whose process died before it could answer.
     */
    static final String PROCESS_EXITED = "process exited";

    String marker;
    String command;
    String rawResult;

    @Getter
    boolean executed = false;

    @Getter
    boolean failed = false;

    @Getter
    String failureReason;

    /**
     * Package private so that tests do not have to wait for the real intervals.
     */
    long pollIntervalMs = 100;

    long timeoutMs = 20_000;

    protected Command(String command) {
        this.command = command;
        this.marker = "@@@@_" + getType() + "_" + MARKER_SEQUENCE.incrementAndGet() + "_@@@@";
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

    /**
     * Marks the command as failed so that {@link #execute} stops waiting for a result that will never come.
     *
     * @param reason what the console reported
     */
    protected void fail(String reason) {
        this.failed = true;
        this.failureReason = reason;
    }

    public Command execute(ProcessThread process, String openttdServerId ) {
        // Everything that was written before this point cannot belong to this command.
        int from = process.getLogsLength();
        // 'echo <marker>' is answered by OpenTTD with the marker itself and, unlike an unknown command, does not
        // produce a "Command '...' not found." line (verified on OpenTTD 15.3). On a build without 'echo' the anchor
        // still works, because the console input is echoed into the logs as well.
        process.write("echo " + this.marker);
        process.write(this.command);

        long deadline = System.currentTimeMillis() + this.timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isFinished(process, from, openttdServerId)) {
                return this;
            }
            if (!process.isAlive()) {
                // The console is gone, so the answer can never arrive. One more poll interval gives the pump the
                // chance to flush what the process wrote before it exited, then the command gives up instead of
                // blocking its caller - a scheduler - until the full timeout has passed.
                if (!sleepPollInterval() || isFinished(process, from, openttdServerId)) {
                    return this;
                }
                fail(PROCESS_EXITED);
                return this;
            }
            if (!sleepPollInterval()) {
                return this;
            }
        }
        return this;
    }

    /**
     * @param from index in the console history this command started at
     * @return true if the command will not change its outcome any more, no matter whether it succeeded or failed
     */
    private boolean isFinished(ProcessThread process, int from, String openttdServerId) {
        String window = process.getLogsFrom(from);
        int beginIndex = window.indexOf(this.marker);
        if (beginIndex > -1) {
            this.rawResult = window.substring(beginIndex);
            if (check(this.rawResult)) {
                this.executed = true;
                this.onSuccess(openttdServerId);
                return true;
            }
        }
        return this.failed;
    }

    /**
     * @return false if the wait was interrupted, in which case the caller has to give up
     */
    private boolean sleepPollInterval() {
        try {
            Thread.sleep(this.pollIntervalMs);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

}
