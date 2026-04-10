package org.telecom_operations_dashboard.alert.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class AlertSseBroadcaster {

    private final List<SseEmitter> highEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> criticalEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerHighStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(highEmitters, "alerts-high-connected");
        log.info("New HIGH alerts SSE client connected.");
        return emitter;
    }

    public SseEmitter registerCriticalStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(criticalEmitters, "alerts-critical-connected");
        log.info("New CRITICAL alerts SSE client connected.");
        return emitter;
    }

    public void broadcastHighAlert(AlertDto alert) {
        SseBroadcaster.broadcastJson(highEmitters, "alerts-high", alert);
    }

    public void broadcastCriticalAlert(AlertDto alert) {
        SseBroadcaster.broadcastJson(criticalEmitters, "alerts-critical", alert);
    }
}