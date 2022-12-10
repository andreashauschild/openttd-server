package de.litexo.model.internal;

import de.litexo.model.external.OpenttdServer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InternalOpenttdServerConfig {
    private String path;
    private int autoSaveMinutes = 5;

    // sha256 hash of the admin password

    private String passwordSha256Hash;

    List<OpenttdServer> servers = new ArrayList<>();
}
