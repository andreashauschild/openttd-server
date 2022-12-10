package de.litexo.model.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.litexo.OpenttdProcess;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OpenttdServer {

    // unique name of the server
    private String name = null;

    // of the server
    private Integer port = null;

    // the save game that is loaded by default
    private ServerFile saveGame = null;

    // auto save of the server
    private ServerFile autoSaveGame = null;

    // if set this save game is used to start the server
    private ServerFile startSaveGame = null;

    // if set this config will be used to start the server
    private ServerFile config = null;

    // holds the server process if the server is running
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private OpenttdProcess process;
}
