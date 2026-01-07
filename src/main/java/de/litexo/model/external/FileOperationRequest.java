package de.litexo.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileOperationRequest {
    String sourcePath;
    String destinationPath;
    Boolean overwrite;
}
