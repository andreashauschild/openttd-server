package de.litexo.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The only message a client sends on '/data-stream'. The first frame of a connection must be one of these, a
 * connection that does not authenticate itself is closed. Later frames switch the subscription to another process.
 *
 * @param type       always 'subscribe'
 * @param sessionId  session id of the logged in admin, the same value the rest api expects in its session header
 * @param processId  uuid of the process thread to follow, null for "every process" (dashboard mode, no snapshot)
 * @param fromOffset absolute offset the client already has, null for "send the tail of the console history"
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscribeMessage(String type, String sessionId, String processId, Long fromOffset) {

    static final String TYPE_SUBSCRIBE = "subscribe";

    public boolean isSubscribe() {
        return TYPE_SUBSCRIBE.equals(this.type);
    }
}
