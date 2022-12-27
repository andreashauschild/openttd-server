package de.litexo.model.external;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OpenttdServerConfigGet {
    private int autoSaveMinutes;
    private int numberOfAutoSaveFilesToKeep;
    private int numberOfManuallySaveFilesToKeep;
    List<OpenttdServer> servers = new ArrayList<>();
}
