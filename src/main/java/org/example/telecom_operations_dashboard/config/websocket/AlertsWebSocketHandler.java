package org.example.telecom_operations_dashboard.config.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;


public class AlertsWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AlertsWebSocketHandler.class);
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("Alerts WebSocket connection established: {}", session.getId());
        session.sendMessage(new TextMessage("{\"status\":\"connected\",\"message\":\"Connected to alerts stream\"}"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.debug("Alerts WebSocket message received from {}: {}", session.getId(), message.getPayload());
        // Handle subscription filters or commands
        session.sendMessage(new TextMessage("{\"status\":\"received\",\"message\":\"Alert filter registered\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        log.info("Alerts WebSocket connection closed: {} - {}", session.getId(), closeStatus.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Alerts WebSocket transport error for {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session);
    }

}
