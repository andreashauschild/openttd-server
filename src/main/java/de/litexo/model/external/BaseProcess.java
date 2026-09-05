package de.litexo.model.external;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseProcess {
    private String processId;
    private String processData;

    /**
     * Exit code of the process, null while it is still running.
     */
    private Integer exitCode;

    /**
     * Number of automatic commands (server_info, pause, unpause, autosave) that were not sent because the terminal is
     * open in the ui.
     */
    private int skippedCommands;
}
