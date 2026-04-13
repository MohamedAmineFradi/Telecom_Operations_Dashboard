package org.telecom_operations_dashboard.call.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CallRawSseBroadcaster {
    private static final int MAX_SSE_CLIENTS = 250;

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final AtomicReference<CallEvent> latestRawEvent = new AtomicReference<>();

    public SseEmitter registerRawStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(rawEmitters, "call-raw-connected", MAX_SSE_CLIENTS);
        CallEvent replay = latestRawEvent.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "call-raw", replay)) {
            rawEmitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcastRawEvent(CallEvent event) {
        latestRawEvent.set(event);
        SseBroadcaster.broadcastJson(rawEmitters, "call-raw", event);
    }
}
