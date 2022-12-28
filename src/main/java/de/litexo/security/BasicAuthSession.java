package de.litexo.security;

import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.core.SecurityContext;
import java.util.UUID;


public class BasicAuthSession {
    @Getter
    private String sessionId = UUID.randomUUID().toString();

    @Getter
    @Setter
    private long lastUpdate;

    @Getter
    @Setter
    private String user;

    @Getter
    @Setter
    private SecurityContext securityContext;
}
