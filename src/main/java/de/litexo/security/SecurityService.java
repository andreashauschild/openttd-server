package de.litexo.security;

import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import io.quarkus.scheduler.Scheduled;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SecurityService {

    @ConfigProperty(name = "server.disable.security", defaultValue = "false")
    boolean disableSecurity;

    @ConfigProperty(name = "server.session.timeout-minutes", defaultValue = "1440")
    long sessionTimeoutMinutes;

    @Inject
    DefaultRepository repository;
    private static final Logger LOG = Logger.getLogger(SecurityService.class);

    /**
     * Current session header. Hyphens only, because nginx drops headers with underscores by default, which made the
     * application answer with 401 behind a reverse proxy.
     */
    public static final String HEADER_OPENTTD_SERVER_SESSION_ID = "X-Openttd-Server-Session-Id";

    /**
     * Header used up to and including the previous release. Still accepted and still sent, so that a browser holding a
     * cached copy of the old ui keeps working for one release.
     */
    public static final String LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID = "X-OPENTTD_SERVER_SESSION_ID";

    Map<String, BasicAuthSession> sessions = new ConcurrentHashMap<>();

    /**
     * @return the new header if it carries a value, the legacy header otherwise
     */
    public static String sessionIdFrom(String newHeader, String legacyHeader) {
        return StringUtils.isNotBlank(newHeader) ? newHeader : legacyHeader;
    }

    public void validatedLoginSession(ContainerRequestContext requestContext) {
        String sessionId = sessionIdFrom(
                requestContext.getHeaders().getFirst(HEADER_OPENTTD_SERVER_SESSION_ID),
                requestContext.getHeaders().getFirst(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID));
        if (sessionId != null && sessions.containsKey(sessionId)) {
            this.sessions.get(sessionId).setLastUpdate(System.currentTimeMillis());
            requestContext.setSecurityContext(this.sessions.get(sessionId).getSecurityContext());
        }
    }

    /**
     * Removes sessions that were not used for 'server.session.timeout-minutes'. The ui refreshes its session every
     * 30 seconds through 'verifyLogin', so an open tab never expires.
     */
    @Scheduled(every = "10m")
    void expireSessions() {
        long maxAgeMs = TimeUnit.MINUTES.toMillis(this.sessionTimeoutMinutes);
        long now = System.currentTimeMillis();
        this.sessions.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().getLastUpdate()) > maxAgeMs;
            if (expired) {
                LOG.infof("Session of user '%s' expired", entry.getValue().getUser());
            }
            return expired;
        });
    }

    public boolean isLoggedIn(String sessionId) {
        return this.disableSecurity || sessionId != null && sessions.containsKey(sessionId);
    }

    /**
     * Marks the session as used. The terminal websocket is open for hours without sending a single http request, so
     * without this the session of an admin who only watches the terminal would expire.
     *
     * @param sessionId session that is still in use, unknown ids are ignored
     */
    public void refreshSession(String sessionId) {
        BasicAuthSession session = sessionId != null ? this.sessions.get(sessionId) : null;
        if (session != null) {
            session.setLastUpdate(System.currentTimeMillis());
        }
    }

    public Optional<BasicAuthSession> login(String authHeaderValue) {
        ;
        if (authHeaderValue != null) {
            BasicAuth auth = new BasicAuth(authHeaderValue);
            InternalOpenttdServerConfig openttdServerConfig = this.repository.getOpenttdServerConfig();
            if (auth != null && SecurityUtils.isEquals(auth.getPassword(), openttdServerConfig.getPasswordSha256Hash()) && "admin".equals(auth.getUserName())) {
                return getBasicAuthSession(auth);
            }
        } else if (this.disableSecurity) {
            // if disabled we just fake a basic auth
            BasicAuth auth = new BasicAuth("Basic YWRtaW46UGFzc3dvcmRfMQ=="); //admin:Password_1
            return getBasicAuthSession(auth);
        }
        return Optional.empty();
    }

    private Optional<BasicAuthSession> getBasicAuthSession(BasicAuth auth) {
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
        LOG.infof("User '%s' logged in",basicAuthSession.getUser());
        this.sessions.put(basicAuthSession.getSessionId(), basicAuthSession);
        return Optional.of(basicAuthSession);
    }

    public void logout(String sessionId) {
        if (sessionId != null && sessions.containsKey(sessionId)) {
            this.sessions.remove(sessionId);
        }
    }
}
