package de.litexo.security;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static de.litexo.security.SecurityService.HEADER_OPENTTD_SERVER_SESSION_ID;


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
            return Response.status(200).header(HEADER_OPENTTD_SERVER_SESSION_ID, login.get().getSessionId()).build();
        }
        return Response.status(401).build();
    }

    @Path("/verifyLogin")
    @POST()
    public Response verifyLogin(@HeaderParam(HEADER_OPENTTD_SERVER_SESSION_ID) String session) {
        if (securityService.isLoggedIn(session)) {
            return Response.status(200).header(HEADER_OPENTTD_SERVER_SESSION_ID, session).build();
        }
        return Response.status(401).build();
    }

    @Path("/logout")
    @POST
    public Response logout(@HeaderParam(HEADER_OPENTTD_SERVER_SESSION_ID) String session) {
        securityService.logout(session);
        return Response.ok().build();
    }


}
