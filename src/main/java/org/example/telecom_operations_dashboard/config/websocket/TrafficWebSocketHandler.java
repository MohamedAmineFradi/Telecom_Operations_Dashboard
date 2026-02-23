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

public class TrafficWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TrafficWebSocketHandler.class);
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("Traffic WebSocket connection established: {}", session.getId());
        session.sendMessage(new TextMessage("{\"status\":\"connected\",\"message\":\"Connected to traffic stream\"}"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.debug("Traffic WebSocket message received from {}: {}", session.getId(), message.getPayload());
        // Echo message back or handle subscription filters
        session.sendMessage(new TextMessage("{\"status\":\"received\",\"message\":\"Message received\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        log.info("Traffic WebSocket connection closed: {} - {}", session.getId(), closeStatus.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Traffic WebSocket transport error for {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session);
    }
}
