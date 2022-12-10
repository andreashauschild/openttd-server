package de.litexo.model.external;

import lombok.Data;

@Data
public class OpenttdServerConfigUpdate {
    private int autoSaveMinutes = 5;

    private String password;

    private String oldPassword;
}
