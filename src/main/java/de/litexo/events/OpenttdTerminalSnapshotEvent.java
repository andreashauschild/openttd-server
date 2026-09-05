package de.litexo.events;

import lombok.Getter;
import lombok.ToString;

/**
 * Answer to a subscription for a single process: the console history the client is missing, followed by
 * {@link OpenttdTerminalUpdateEvent}s. The text ends at {@link #getEndOffset()}, so a client drops every update whose
 * {@link OpenttdTerminalUpdateEvent#getOffset()} is smaller than that and appends the rest.
 */
@ToString(callSuper = true)
public class OpenttdTerminalSnapshotEvent extends BaseEvent {

    @Getter
    private String processId;

    @Getter
    private String text;

    /**
     * Absolute offset right after {@link #getText()}, i.e. the current end of the console history of the process.
     */
    @Getter
    private long endOffset;

    /**
     * Exit code of the process, {@code null} while it is alive.
     */
    @Getter
    private Integer exitCode;

    public OpenttdTerminalSnapshotEvent(Object eventSource, String processId, String text, long endOffset, Integer exitCode) {
        super(eventSource);
        this.processId = processId;
        this.text = text;
        this.endOffset = endOffset;
        this.exitCode = exitCode;
    }
}
