package org.example.producer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.SimulationTickEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SimulationClockSseBroadcaster {

    private static final int MAX_SSE_CLIENTS = 200;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final TimeAuthorityService timeAuthorityService;

    public SimulationClockSseBroadcaster(TimeAuthorityService timeAuthorityService) {
        this.timeAuthorityService = timeAuthorityService;
    }

    public SseEmitter registerStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(emitters, "simulation-clock-connected", MAX_SSE_CLIENTS);

        SimulationTickEvent replay = timeAuthorityService.getLatestSimulationTick();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "simulation-clock", replay)) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(SimulationTickEvent tick) {
        SseBroadcaster.broadcastJson(emitters, "simulation-clock", tick);
    }
}
