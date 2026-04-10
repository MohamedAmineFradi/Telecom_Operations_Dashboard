package org.telecom_operations_dashboard.mobility.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class MobilityRawSseBroadcaster {

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerRawStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(rawEmitters, "mobility-raw-connected");
        log.info("New raw Mobility SSE client connected.");
        return emitter;
    }

    public void broadcastRawEvent(MobilityEvent event) {
        SseBroadcaster.broadcastJson(rawEmitters, "mobility-raw", event);
    }
}
