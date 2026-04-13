package org.telecom_operations_dashboard.mobility.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class MobilityRawSseBroadcaster {
    private static final int MAX_SSE_CLIENTS = 250;

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final AtomicReference<MobilityEvent> latestRawEvent = new AtomicReference<>();

    public SseEmitter registerRawStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(rawEmitters, "mobility-raw-connected", MAX_SSE_CLIENTS);
        MobilityEvent replay = latestRawEvent.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "mobility-raw", replay)) {
            rawEmitters.remove(emitter);
        }
        log.info("New raw Mobility SSE client connected.");
        return emitter;
    }

    public void broadcastRawEvent(MobilityEvent event) {
        latestRawEvent.set(event);
        SseBroadcaster.broadcastJson(rawEmitters, "mobility-raw", event);
    }
}
