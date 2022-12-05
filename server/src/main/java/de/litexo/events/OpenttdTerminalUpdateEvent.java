package de.litexo.events;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)

public class OpenttdTerminalUpdateEvent extends BaseEvent {

    @Getter
    private String processId;

    @Getter
    private String text;

    public OpenttdTerminalUpdateEvent(Object eventSource, String processId, String text) {
        super(eventSource);
        this.processId = processId;
        this.text = text;
    }
}
