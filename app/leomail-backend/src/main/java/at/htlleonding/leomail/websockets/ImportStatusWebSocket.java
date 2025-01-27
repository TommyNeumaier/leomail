package at.htlleonding.leomail.websockets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;
import at.htlleonding.leomail.services.ImportStatusService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/ws/import-status")
@ApplicationScoped
public class ImportStatusWebSocket {


    private static final Logger LOGGER = Logger.getLogger(ImportStatusWebSocket.class);
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("WebSocket geöffnet: " + session.getId());
        sessions.add(session);
        sendStatus(session, ImportStatusService.getInstance().isImporting());
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("WebSocket geschlossen: " + session.getId());
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("WebSocket-Fehler bei Session " + session.getId(), throwable);
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Nachricht von " + session.getId() + ": " + message);
    }

    /**
     * Sendet den aktuellen Importstatus an alle verbundenen Clients.
     *
     * @param importing Der aktuelle Importstatus
     */
    public static void broadcastStatus(boolean importing) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText("{\"importing\": " + importing + "}");
                    } catch (IOException e) {
                        LOGGER.error("Fehler beim Senden der Nachricht an Session " + session.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * Sendet den Importstatus an eine spezifische Session.
     *
     * @param session    Die WebSocket-Session
     * @param importing Der aktuelle Importstatus
     */
    private void sendStatus(Session session, boolean importing) {
        try {
            session.getBasicRemote().sendText("{\"importing\": " + importing + "}");
        } catch (IOException e) {
            LOGGER.error("Fehler beim Senden des Initialstatus an Session " + session.getId(), e);
        }
    }
}
