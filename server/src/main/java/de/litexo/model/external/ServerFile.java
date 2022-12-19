package de.litexo.model.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ServerFile {

    // This value represents an owner id of the given file
    private String ownerId;

    private String path;

    private String name;

    private ServerFileType type;


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean exists = true;


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long created;


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long lastModified;
}
