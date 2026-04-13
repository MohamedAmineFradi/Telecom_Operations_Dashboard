package org.telecom_operations_dashboard.internet.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class InternetRawSseBroadcaster {
    private static final int MAX_SSE_CLIENTS = 250;

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final AtomicReference<InternetEvent> latestRawEvent = new AtomicReference<>();

    public SseEmitter registerRawStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(rawEmitters, "internet-raw-connected", MAX_SSE_CLIENTS);
        InternetEvent replay = latestRawEvent.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "internet-raw", replay)) {
            rawEmitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcastRawEvent(InternetEvent event) {
        latestRawEvent.set(event);
        SseBroadcaster.broadcastJson(rawEmitters, "internet-raw", event);
    }
}
