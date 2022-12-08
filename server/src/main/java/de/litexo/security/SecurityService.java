package de.litexo.security;

import de.litexo.model.OpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class SecurityService {
    @Inject
    DefaultRepository repository;
    private static final Logger LOG = Logger.getLogger(SecurityService.class);
    public static final String HEADER_OPENTTD_SERVER_SESSION_ID = "X-OPENTTD_SERVER_SESSION_ID";
    Map<String, BasicAuthSession> sessions = new HashMap<>();

    public void validatedLoginSession(ContainerRequestContext requestContext) {
        Cookie cookie = requestContext.getCookies().get(HEADER_OPENTTD_SERVER_SESSION_ID);
        if (cookie != null) {
            if (sessions.containsKey(cookie.getValue())) {
                this.sessions.get(cookie.getValue()).setLastUpdate(System.currentTimeMillis());
                requestContext.setSecurityContext(this.sessions.get(cookie.getValue()).getSecurityContext());
            }
        }
    }

    public Optional<BasicAuthSession> login(String authHeaderValue) {
        BasicAuth auth = null;
        if (authHeaderValue != null) {
            auth = new BasicAuth(authHeaderValue);
        }
        OpenttdServerConfig openttdServerConfig = this.repository.getOpenttdServerConfig();
        if (SecurityUtils.isEquals(auth.getPassword(), openttdServerConfig.getPasswordSha256Hash()) && "admin".equals(auth.getUserName())) {
            final BasicAuthSession basicAuthSession = new BasicAuthSession();
            basicAuthSession.setLastUpdate(System.currentTimeMillis());
            basicAuthSession.setUser(auth.getUserName());
            basicAuthSession.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return new Principal() {
                        @Override
                        public String getName() {
                            return basicAuthSession.getUser();
                        }
                    };
                }

                @Override
                public boolean isUserInRole(String r) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String getAuthenticationScheme() {
                    return "basic";
                }
            });
            LOG.infof("User '%s' logged in");
            this.sessions.put(basicAuthSession.getSessionId(), basicAuthSession);
            return Optional.of(basicAuthSession);
        }
        return Optional.empty();
    }

    public void logout(String sessionId) {
        if (sessionId != null) {
            if (sessions.containsKey(sessionId)) {
                this.sessions.remove(sessionId);
            }
        }
    }
}
