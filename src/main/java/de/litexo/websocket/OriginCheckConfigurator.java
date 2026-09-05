package de.litexo.websocket;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Optional protection of '/data-stream' against a page on another origin opening the socket (cross site websocket
 * hijacking). It is off by default and the real protection is the session id in the first frame: a foreign page cannot
 * read the session id out of the local storage of the application, so its connection is closed after the timeout.
 * <p>
 * Turn it on with 'server.websocket.check-origin=true' and list the origins the web app is served from in
 * 'server.websocket.allowed-origins' (comma separated, for example 'https://openttd.example.com'). A handshake without
 * an 'Origin' header - every non browser client - is always accepted, a handshake with an unlisted origin is rejected.
 * <p>
 * The check cannot simply compare the origin with the host of the request: the websocket container calls
 * {@link #checkOrigin(String)} while it matches the handshake and hands over the 'Origin' header only. That is also
 * why the check has to stay off by default: with the Angular dev server the browser origin is 'http://localhost:4200'
 * while the backend answers on ':8080', so a same origin rule would break development.
 */
public class OriginCheckConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger LOG = Logger.getLogger(OriginCheckConfigurator.class);

    static final String CHECK_ORIGIN = "server.websocket.check-origin";

    static final String ALLOWED_ORIGINS = "server.websocket.allowed-origins";

    private final boolean checkOrigin;

    private final List<String> allowedOrigins;

    public OriginCheckConfigurator() {
        this(ConfigProvider.getConfig().getOptionalValue(CHECK_ORIGIN, Boolean.class).orElse(false),
                ConfigProvider.getConfig().getOptionalValue(ALLOWED_ORIGINS, String.class).orElse(""));
    }

    OriginCheckConfigurator(boolean checkOrigin, String allowedOrigins) {
        this.checkOrigin = checkOrigin;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
        if (this.checkOrigin) {
            LOG.infof("Origin check of the '/data-stream' websocket is enabled, allowed origins: %s", this.allowedOrigins);
        }
    }

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        if (!this.checkOrigin || originHeaderValue == null || originHeaderValue.isBlank()) {
            return true;
        }
        boolean allowed = this.allowedOrigins.contains(originHeaderValue.trim());
        if (!allowed) {
            LOG.warnf("Rejected a '/data-stream' handshake from origin '%s'", originHeaderValue);
        }
        return allowed;
    }
}
