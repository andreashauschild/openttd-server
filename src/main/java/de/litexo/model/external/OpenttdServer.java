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

    // Password of the server.
    private String password = null;

    // of the server
    private Integer port = null;

    // the save game that is loaded by default. this will be set on application startup to the newest save game that belongs to this server on
    private ServerFile saveGame = null;

    // if set this config will be used to start the server
    private ServerFile openttdConfig = null;


    // if set this is the private config that will be used to start the server
    private ServerFile openttdPrivateConfig = null;

    // if set this is used to set the secret config of the server
    private ServerFile openttdSecretsConfig = null;

    // Enables auto save for this server
    private boolean autoSave = true;

    // Enables autoPause for this server if no played has joined a company
    private boolean autoPause = true;

    // State flag that is set if a pause action was executed
    private boolean paused = false;

    // Server Info Command result
    private String inviteCode;

    // Server Info Command result
    private int currentClients;

    // Server Info Command result
    private int maxClients;

    // Server Info Command result
    private int currentCompanies;

    // Server Info Command result
    private int maxCompanies;

    // Server Info Command result
    private int currentSpectators;

    // holds the server process if the server is running
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private OpenttdProcess process;


}
