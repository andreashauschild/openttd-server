package de.litexo.model.external;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ExplorerData {
    String root;
    String fileSeperator;
    List<ExplorerDirectory> directories = new ArrayList<>();
}
