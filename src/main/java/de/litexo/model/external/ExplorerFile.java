package de.litexo.model.external;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ExplorerFile {
    String id;
    String name;
    String absolutePath;
    String baseName;
    String extension;
}
