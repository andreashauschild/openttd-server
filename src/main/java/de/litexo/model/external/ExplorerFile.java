package de.litexo.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExplorerFile {
    String id;
    String name;
    String absolutePath;
    String relativePath;
    String baseName;
    String extension;
    Long size;
    Long lastModified;
    Boolean isDirectory;
}
