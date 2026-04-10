package org.telecom_operations_dashboard.internet.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InternetRawSseBroadcaster {

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerRawStream() {
        return SseBroadcaster.registerEmitter(rawEmitters, "internet-raw-connected");
    }

    public void broadcastRawEvent(InternetEvent event) {
        SseBroadcaster.broadcastJson(rawEmitters, "internet-raw", event);
    }
}
