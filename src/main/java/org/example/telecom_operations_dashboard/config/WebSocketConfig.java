package org.example.telecom_operations_dashboard.config;

import org.example.telecom_operations_dashboard.config.websocket.AlertsWebSocketHandler;
import org.example.telecom_operations_dashboard.config.websocket.TrafficWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.WebSocketHandler;

/**
 * WebSocket configuration for real-time traffic and alerts streaming
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(trafficWebSocketHandler(), "/ws/traffic")
                .setAllowedOrigins("*");
        registry.addHandler(alertsWebSocketHandler(), "/ws/alerts")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler trafficWebSocketHandler() {
        return new TrafficWebSocketHandler();
    }

    @Bean
    public WebSocketHandler alertsWebSocketHandler() {
        return new AlertsWebSocketHandler();
    }
}
