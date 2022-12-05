package de.litexo.security;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static de.litexo.security.SecurityService.OPENTTD_SERVER_SESSION_ID;
import static javax.ws.rs.core.Cookie.DEFAULT_VERSION;
import static javax.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;


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
            // Info: it is important to set the path of the cookie to root. In that case the cookie can be accessed on every route.
            NewCookie newCookie = new NewCookie(OPENTTD_SERVER_SESSION_ID, login.get().getSessionId(), "/", null, DEFAULT_VERSION, null, DEFAULT_MAX_AGE, null, false, false);
            return Response.status(200).cookie(newCookie).build();
        }
        return Response.status(401).build();
    }

    @Path("/logout")
    @POST
    public Response logout(@CookieParam(OPENTTD_SERVER_SESSION_ID) Cookie cookie) {
        securityService.logout(cookie);
        return Response.ok().build();
    }


}
