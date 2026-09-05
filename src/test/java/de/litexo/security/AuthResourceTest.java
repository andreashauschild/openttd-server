package de.litexo.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static de.litexo.security.SecurityService.HEADER_OPENTTD_SERVER_SESSION_ID;
import static de.litexo.security.SecurityService.LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthResourceTest {

    private static final String BASIC_AUTH_HEADER = "Basic YWRtaW46UGFzc3dvcmRfMQ==";

    @Mock
    SecurityService securityService;

    @InjectMocks
    AuthResource subject = new AuthResource();

    @DisplayName("Test that the login response carries the new and the legacy session header")
    @Test
    void test_loginResponseCarriesBothHeaders() {
        BasicAuthSession session = new BasicAuthSession();
        session.setUser("admin");
        when(this.securityService.login(BASIC_AUTH_HEADER)).thenReturn(Optional.of(session));

        Response response = this.subject.login(BASIC_AUTH_HEADER);

        assertEquals(200, response.getStatus());
        assertEquals(session.getSessionId(), response.getHeaderString(HEADER_OPENTTD_SERVER_SESSION_ID));
        assertEquals(session.getSessionId(), response.getHeaderString(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID));
    }

    @DisplayName("Test that a failed login is answered with 401")
    @Test
    void test_loginFailureIsUnauthorized() {
        when(this.securityService.login("Basic wrong")).thenReturn(Optional.empty());

        assertEquals(401, this.subject.login("Basic wrong").getStatus());
    }

    @DisplayName("Test that verifyLogin works with the legacy header only")
    @Test
    void test_verifyLoginAcceptsLegacyHeaderOnly() {
        when(this.securityService.isLoggedIn("session-1")).thenReturn(true);

        Response response = this.subject.verifyLogin(null, "session-1");

        assertEquals(200, response.getStatus());
        assertEquals("session-1", response.getHeaderString(HEADER_OPENTTD_SERVER_SESSION_ID));
        assertEquals("session-1", response.getHeaderString(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID));
    }

    @DisplayName("Test that verifyLogin works with the new header only")
    @Test
    void test_verifyLoginAcceptsNewHeaderOnly() {
        when(this.securityService.isLoggedIn("session-2")).thenReturn(true);

        assertEquals(200, this.subject.verifyLogin("session-2", null).getStatus());
    }

    @DisplayName("Test that logout uses the session id of either header")
    @Test
    void test_logoutAcceptsLegacyHeader() {
        this.subject.logout(null, "session-3");

        verify(this.securityService).logout("session-3");
    }
}
