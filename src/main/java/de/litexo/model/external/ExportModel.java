package de.litexo.model.external;

import de.litexo.commands.PauseCommand;
import de.litexo.commands.UnpauseCommand;
import de.litexo.events.OpenttdTerminalUpdateEvent;
import lombok.Data;

@Data
public class ExportModel {
    private OpenttdTerminalUpdateEvent openttdTerminalUpdateEvent;
    private UnpauseCommand unpauseCommand;
    private PauseCommand pauseCommand;

    private ServiceError serviceError;
}
