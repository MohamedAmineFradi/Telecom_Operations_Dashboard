package org.telecom_operations_dashboard.call.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CallRawSseBroadcaster {

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerRawStream() {
        return SseBroadcaster.registerEmitter(rawEmitters, "call-raw-connected");
    }

    public void broadcastRawEvent(CallEvent event) {
        SseBroadcaster.broadcastJson(rawEmitters, "call-raw", event);
    }
}
