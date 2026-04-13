package org.telecom_operations_dashboard.alert.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class AlertSseBroadcaster {
    private static final int MAX_SSE_CLIENTS = 250;

    private final List<SseEmitter> highEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> criticalEmitters = new CopyOnWriteArrayList<>();
    private final AtomicReference<AlertDto> latestHighAlert = new AtomicReference<>();
    private final AtomicReference<AlertDto> latestCriticalAlert = new AtomicReference<>();

    public SseEmitter registerHighStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(highEmitters, "alerts-high-connected", MAX_SSE_CLIENTS);
        AlertDto replay = latestHighAlert.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "alerts-high", replay)) {
            highEmitters.remove(emitter);
        }
        log.info("New HIGH alerts SSE client connected.");
        return emitter;
    }

    public SseEmitter registerCriticalStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(criticalEmitters, "alerts-critical-connected", MAX_SSE_CLIENTS);
        AlertDto replay = latestCriticalAlert.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "alerts-critical", replay)) {
            criticalEmitters.remove(emitter);
        }
        log.info("New CRITICAL alerts SSE client connected.");
        return emitter;
    }

    public void broadcastHighAlert(AlertDto alert) {
        latestHighAlert.set(alert);
        SseBroadcaster.broadcastJson(highEmitters, "alerts-high", alert);
    }

    public void broadcastCriticalAlert(AlertDto alert) {
        latestCriticalAlert.set(alert);
        SseBroadcaster.broadcastJson(criticalEmitters, "alerts-critical", alert);
    }

    public void broadcastCriticalTransition(AlertDto alert, String fromSeverity) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "CRITICAL_TRANSITION");
        payload.put("fromSeverity", fromSeverity);
        payload.put("toSeverity", "CRITICAL");
        payload.put("alert", alert);

        SseBroadcaster.broadcastJson(criticalEmitters, "alerts-critical-transition", payload);
        SseBroadcaster.broadcastJson(highEmitters, "alerts-critical-transition", payload);
    }

    public void broadcastResolvedAlert(AlertDto alert) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "RESOLVED");
        payload.put("id", alert.id());
        payload.put("cellId", alert.cellId());
        payload.put("severity", alert.severity());
        payload.put("timestamp", alert.timestamp());

        SseBroadcaster.broadcastJson(highEmitters, "alerts-resolved", payload);
        SseBroadcaster.broadcastJson(criticalEmitters, "alerts-resolved", payload);
    }
}