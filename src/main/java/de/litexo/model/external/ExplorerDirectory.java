package de.litexo.model.external;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ExplorerDirectory {
    String id;
    String name;
    String absolutePath;
    List<ExplorerFile> files = new ArrayList<>();
}
