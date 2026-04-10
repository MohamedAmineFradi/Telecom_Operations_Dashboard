package org.telecom_operations_dashboard.sms.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SmsRawSseBroadcaster {

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerRawStream() {
        return SseBroadcaster.registerEmitter(rawEmitters, "sms-raw-connected");
    }

    public void broadcastRawEvent(SmsEvent event) {
        SseBroadcaster.broadcastJson(rawEmitters, "sms-raw", event);
    }
}
