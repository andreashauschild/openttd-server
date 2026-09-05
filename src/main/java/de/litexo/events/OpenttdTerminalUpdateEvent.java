package de.litexo.events;

import lombok.Getter;
import lombok.ToString;

/**
 * One batch of console output of a process. The batches of a process are contiguous: the text of a batch starts at
 * {@link #getOffset()} and the next batch starts at {@code offset + text.length()}.
 */
@ToString(callSuper = true)

public class OpenttdTerminalUpdateEvent extends BaseEvent {

    @Getter
    private String processId;

    @Getter
    private String text;

    /**
     * Absolute number of characters the process produced before this batch. Absolute means "counted since the process
     * was started", so the number stays valid when the console history is trimmed.
     */
    @Getter
    private long offset;

    /**
     * Exit code of the process, {@code null} while it is alive. Only the last batch of a process, the one that carries
     * the synthetic exit line, has it set.
     */
    @Getter
    private Integer exitCode;

    public OpenttdTerminalUpdateEvent(Object eventSource, String processId, String text) {
        this(eventSource, processId, text, 0, null);
    }

    public OpenttdTerminalUpdateEvent(Object eventSource, String processId, String text, long offset, Integer exitCode) {
        super(eventSource);
        this.processId = processId;
        this.text = text;
        this.offset = offset;
        this.exitCode = exitCode;
    }
}
