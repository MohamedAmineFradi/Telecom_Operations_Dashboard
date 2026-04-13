package org.example.producer.service;

import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.SimulationTickEvent;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TimeAuthorityService {

    private final AtomicReference<SimulationTickEvent> latestSimulationTick = new AtomicReference<>();

    public void updateFromSimulationTick(SimulationTickEvent tick) {
        if (tick != null) {
            latestSimulationTick.set(tick);
        }
    }

    public SimulationTickEvent getLatestSimulationTick() {
        return latestSimulationTick.get();
    }

    public AuthoritativeTime getAuthoritativeTime() {
        SimulationTickEvent tick = latestSimulationTick.get();
        if (tick != null && tick.getTimestamp() != null) {
            return new AuthoritativeTime(tick.getTimestamp().toString(), "simulation-clock");
        }

        return new AuthoritativeTime(OffsetDateTime.now(ZoneOffset.UTC).toString(), "system-clock");
    }

    public record AuthoritativeTime(String timestamp, String source) {
    }
}