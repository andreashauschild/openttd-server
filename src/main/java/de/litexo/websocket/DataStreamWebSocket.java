package de.litexo.websocket;

import de.litexo.events.OpenttdTerminalUpdateEvent;
import de.litexo.events.EventBus;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/data-stream")
@ApplicationScoped
public class DataStreamWebSocket {

    Map<String, Session> sessions = new ConcurrentHashMap<>();


    @Inject
    EventBus eventBus;

    @PostConstruct
    void init() {
        this.eventBus.observe(OpenttdTerminalUpdateEvent.class, this, e -> this.broadcast(e.toJson()));
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session.getId());

    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {

    }

    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }
}
