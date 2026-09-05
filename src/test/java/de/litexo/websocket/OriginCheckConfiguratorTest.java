package de.litexo.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OriginCheckConfiguratorTest {

    @DisplayName("Test that every handshake is accepted while the check is disabled")
    @Test
    void test_disabledCheckAcceptsEveryOrigin() {
        OriginCheckConfigurator subject = new OriginCheckConfigurator(false, "https://openttd.example.com");

        assertTrue(subject.checkOrigin("https://an-attacker.example.com"));
        assertTrue(subject.checkOrigin(null));
    }

    @DisplayName("Test that the enabled check accepts the configured origins and handshakes without an origin")
    @Test
    void test_enabledCheckAcceptsConfiguredOrigins() {
        OriginCheckConfigurator subject = new OriginCheckConfigurator(true, "https://openttd.example.com, http://localhost:4200");

        assertTrue(subject.checkOrigin("https://openttd.example.com"));
        assertTrue(subject.checkOrigin("http://localhost:4200"));
        // Not a browser, there is nothing to hijack
        assertTrue(subject.checkOrigin(null));
        assertTrue(subject.checkOrigin(" "));
    }

    @DisplayName("Test that the enabled check rejects an origin that is not configured")
    @Test
    void test_enabledCheckRejectsForeignOrigins() {
        OriginCheckConfigurator subject = new OriginCheckConfigurator(true, "https://openttd.example.com");

        assertFalse(subject.checkOrigin("https://an-attacker.example.com"));
        assertFalse(subject.checkOrigin("http://openttd.example.com"));
        // Nothing is accepted without a list, the check cannot guess the origin the ui is served from
        assertFalse(new OriginCheckConfigurator(true, "").checkOrigin("https://openttd.example.com"));
    }
}
