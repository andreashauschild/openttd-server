package de.litexo.security;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static de.litexo.security.SecurityService.HEADER_OPENTTD_SERVER_SESSION_ID;
import static de.litexo.security.SecurityService.LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID;
import static de.litexo.security.SecurityService.sessionIdFrom;


@Path("/api/auth")
@PermitAll
public class AuthResource {

    @Inject
    SecurityService securityService;

    @Path("/login")
    @POST()
    public Response login(@HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {
        Optional<BasicAuthSession> login = securityService.login(authHeader);
        if (login.isPresent()) {
            // Both headers for one release: a browser with a cached copy of the old ui still reads the legacy one
            return Response.status(200)
                    .header(HEADER_OPENTTD_SERVER_SESSION_ID, login.get().getSessionId())
                    .header(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID, login.get().getSessionId())
                    .build();
        }
        return Response.status(401).build();
    }

    @Path("/verifyLogin")
    @POST()
    public Response verifyLogin(@HeaderParam(HEADER_OPENTTD_SERVER_SESSION_ID) String session,
                                @HeaderParam(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID) String legacySession) {
        String sessionId = sessionIdFrom(session, legacySession);
        if (securityService.isLoggedIn(sessionId)) {
            return Response.status(200)
                    .header(HEADER_OPENTTD_SERVER_SESSION_ID, sessionId)
                    .header(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID, sessionId)
                    .build();
        }
        return Response.status(401).build();
    }

    @Path("/logout")
    @POST
    public Response logout(@HeaderParam(HEADER_OPENTTD_SERVER_SESSION_ID) String session,
                           @HeaderParam(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID) String legacySession) {
        securityService.logout(sessionIdFrom(session, legacySession));
        return Response.ok().build();
    }


}
