package de.litexo.model.external;

import lombok.Data;

@Data
public class OpenttdServerConfigUpdate {
    private int autoSaveMinutes;
    private int numberOfAutoSaveFilesToKeep;
    private int numberOfManuallySaveFilesToKeep;

    private String password;

    private String oldPassword;


}
