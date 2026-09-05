package de.litexo.security;

import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.litexo.security.SecurityService.HEADER_OPENTTD_SERVER_SESSION_ID;
import static de.litexo.security.SecurityService.LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecurityServiceTest {

    // admin:Password_1
    private static final String BASIC_AUTH_HEADER = "Basic YWRtaW46UGFzc3dvcmRfMQ==";

    @Mock
    DefaultRepository repository;

    @Mock
    ContainerRequestContext requestContext;

    @InjectMocks
    SecurityService subject = new SecurityService();

    private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

    @BeforeEach
    void beforeEach() {
        this.subject.sessionTimeoutMinutes = 1440;
        InternalOpenttdServerConfig config = new InternalOpenttdServerConfig();
        config.setPasswordSha256Hash(SecurityUtils.toSHA256("Password_1"));
        when(this.repository.getOpenttdServerConfig()).thenReturn(config);
        when(this.requestContext.getHeaders()).thenReturn(this.headers);
    }

    private String login() {
        Optional<BasicAuthSession> session = this.subject.login(BASIC_AUTH_HEADER);
        assertTrue(session.isPresent());
        return session.get().getSessionId();
    }

    @DisplayName("Test that the hyphenated session header is accepted")
    @Test
    void test_acceptsNewHeader() {
        String sessionId = login();
        this.headers.putSingle(HEADER_OPENTTD_SERVER_SESSION_ID, sessionId);

        this.subject.validatedLoginSession(this.requestContext);

        verify(this.requestContext).setSecurityContext(this.subject.sessions.get(sessionId).getSecurityContext());
    }

    @DisplayName("Test that the legacy session header is still accepted")
    @Test
    void test_acceptsLegacyHeader() {
        String sessionId = login();
        this.headers.putSingle(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID, sessionId);

        this.subject.validatedLoginSession(this.requestContext);

        verify(this.requestContext).setSecurityContext(this.subject.sessions.get(sessionId).getSecurityContext());
    }

    @DisplayName("Test that the new header wins if both are present")
    @Test
    void test_newHeaderWinsWhenBothPresent() {
        String sessionId = login();
        this.headers.putSingle(HEADER_OPENTTD_SERVER_SESSION_ID, sessionId);
        this.headers.putSingle(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID, "an-unknown-session");

        this.subject.validatedLoginSession(this.requestContext);

        ArgumentCaptor<SecurityContext> captor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(this.requestContext).setSecurityContext(captor.capture());
        assertEquals("admin", captor.getValue().getUserPrincipal().getName());
        assertEquals(sessionId, SecurityService.sessionIdFrom(sessionId, "an-unknown-session"));
    }

    @DisplayName("Test that an unknown session id is not accepted")
    @Test
    void test_unknownSessionIsRejected() {
        this.headers.putSingle(HEADER_OPENTTD_SERVER_SESSION_ID, "an-unknown-session");

        this.subject.validatedLoginSession(this.requestContext);

        verify(this.requestContext, never()).setSecurityContext(org.mockito.ArgumentMatchers.any());
        assertFalse(this.subject.isLoggedIn("an-unknown-session"));
    }

    @DisplayName("Test that a session that was not used for the configured time is removed")
    @Test
    void test_sessionExpiresAfterTimeout() {
        String sessionId = login();
        assertTrue(this.subject.isLoggedIn(sessionId));

        this.subject.sessions.get(sessionId).setLastUpdate(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1441));
        this.subject.expireSessions();

        assertFalse(this.subject.isLoggedIn(sessionId));
        assertTrue(this.subject.sessions.isEmpty());
    }

    @DisplayName("Test that a refreshed session survives the cleanup, the terminal websocket sends no http request")
    @Test
    void test_refreshSessionKeepsTheSessionAlive() {
        String sessionId = login();
        this.subject.sessions.get(sessionId).setLastUpdate(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1441));

        this.subject.refreshSession(sessionId);
        this.subject.expireSessions();

        assertTrue(this.subject.isLoggedIn(sessionId));
    }

    @DisplayName("Test that refreshing an unknown session does nothing")
    @Test
    void test_refreshSessionIgnoresUnknownSessions() {
        this.subject.refreshSession(null);
        this.subject.refreshSession("an-unknown-session");

        assertTrue(this.subject.sessions.isEmpty());
    }

    @DisplayName("Test that a session that is still in use survives the cleanup")
    @Test
    void test_sessionInUseIsKept() {
        String sessionId = login();

        this.subject.expireSessions();

        assertTrue(this.subject.isLoggedIn(sessionId));
    }
}
