package org.telecom_operations_dashboard.mobility.streaming.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class MobilityRawSseBroadcaster {

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerRawStream() {
        SseEmitter emitter = new SseEmitter(0L);
        rawEmitters.add(emitter);

        Runnable cleanup = () -> rawEmitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name("mobility-raw-connected")
                    .data("connected", MediaType.TEXT_PLAIN));
        } catch (IOException | IllegalStateException ex) {
            cleanup.run();
        }

        log.info("New raw Mobility SSE client connected.");
        return emitter;
    }

    public void broadcastRawEvent(MobilityEvent event) {
        if (event == null) {
            return;
        }

        for (SseEmitter emitter : rawEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("mobility-raw")
                        .data(event, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException ex) {
                rawEmitters.remove(emitter);
            }
        }
    }
}
