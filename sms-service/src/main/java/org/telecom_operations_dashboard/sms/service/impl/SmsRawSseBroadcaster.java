package org.telecom_operations_dashboard.sms.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SmsRawSseBroadcaster {
    private static final int MAX_SSE_CLIENTS = 250;

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final AtomicReference<SmsEvent> latestRawEvent = new AtomicReference<>();

    public SseEmitter registerRawStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(rawEmitters, "sms-raw-connected", MAX_SSE_CLIENTS);
        SmsEvent replay = latestRawEvent.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "sms-raw", replay)) {
            rawEmitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcastRawEvent(SmsEvent event) {
        latestRawEvent.set(event);
        SseBroadcaster.broadcastJson(rawEmitters, "sms-raw", event);
    }
}
