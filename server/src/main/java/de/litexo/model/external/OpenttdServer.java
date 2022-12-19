package de.litexo.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.litexo.OpenttdProcess;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenttdServer {

    // unique id of the erver
    private String id = UUID.randomUUID().toString();

    // Name of the server
    private String name = null;

    // of the server
    private Integer port = null;

    // the save game that is loaded by default. this will be set on application startup to the newest save game that belongs to this server on
    private ServerFile saveGame = null;

    // if set this config will be used to start the server
    private ServerFile config = null;

    // holds the server process if the server is running
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private OpenttdProcess process;


}
