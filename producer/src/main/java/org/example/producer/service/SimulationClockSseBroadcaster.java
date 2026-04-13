package org.example.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.SimulationTickEvent;

import java.io.IOException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SimulationClockSseBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SimulationClockSseBroadcaster.class);
    private static final int MAX_SSE_CLIENTS = 200;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final TimeAuthorityService timeAuthorityService;

    public SimulationClockSseBroadcaster(TimeAuthorityService timeAuthorityService) {
        this.timeAuthorityService = timeAuthorityService;
    }

    public SseEmitter registerStream() {
        SseEmitter emitter = new SseEmitter(0L);

        while (emitters.size() >= MAX_SSE_CLIENTS) {
            SseEmitter dropped = emitters.get(0);
            emitters.remove(dropped);
            try {
                dropped.complete();
            } catch (Exception ignored) {
                // no-op
            }
        }

        emitters.add(emitter);

        Runnable cleanup = () -> emitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name("simulation-clock-connected")
                    .data("connected", MediaType.TEXT_PLAIN));

            SimulationTickEvent replay = timeAuthorityService.getLatestSimulationTick();
            if (replay != null) {
                emitter.send(SseEmitter.event()
                        .name("simulation-clock")
                        .data(replay, MediaType.APPLICATION_JSON)
                        .id(String.valueOf(System.currentTimeMillis())));
            }
        } catch (IOException | IllegalStateException ex) {
            cleanup.run();
        }

        return emitter;
    }

    public void broadcast(SimulationTickEvent tick) {
        if (tick == null) {
            return;
        }

        if (emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("simulation-clock")
                        .data(tick, MediaType.APPLICATION_JSON)
                        .id(String.valueOf(System.currentTimeMillis())));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
            } catch (Exception ex) {
                log.warn("Failed to emit simulation clock tick: {}", ex.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}
