package de.litexo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ServerFile {

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
