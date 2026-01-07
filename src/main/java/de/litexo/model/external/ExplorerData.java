package de.litexo.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExplorerData {
    String root;
    String fileSeperator;
    List<ExplorerDirectory> directories = new ArrayList<>();
}
