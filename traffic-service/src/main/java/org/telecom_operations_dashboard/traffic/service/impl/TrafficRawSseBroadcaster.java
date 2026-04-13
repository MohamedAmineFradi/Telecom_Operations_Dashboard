package org.telecom_operations_dashboard.traffic.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
import org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TrafficRawSseBroadcaster {
    private static final int MAX_SSE_CLIENTS = 300;

    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> heatmapEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> congestionEmitters = new CopyOnWriteArrayList<>();

    private final AtomicReference<TrafficEvent> latestRawEvent = new AtomicReference<>();
    private final AtomicReference<HourlyTrafficDto> latestHeatmapEvent = new AtomicReference<>();
    private final AtomicReference<CongestionCellDto> latestCongestionEvent = new AtomicReference<>();

    public SseEmitter registerRawStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(rawEmitters, "traffic-raw-connected", MAX_SSE_CLIENTS);
        TrafficEvent replay = latestRawEvent.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "traffic-raw", replay)) {
            rawEmitters.remove(emitter);
        }
        return emitter;
    }

    public SseEmitter registerHeatmapStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(heatmapEmitters, "traffic-heatmap-connected", MAX_SSE_CLIENTS);
        HourlyTrafficDto replay = latestHeatmapEvent.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "traffic-heatmap-update", replay)) {
            heatmapEmitters.remove(emitter);
        }
        return emitter;
    }

    public SseEmitter registerCongestionStream() {
        SseEmitter emitter = SseBroadcaster.registerEmitter(congestionEmitters, "traffic-congestion-connected", MAX_SSE_CLIENTS);
        CongestionCellDto replay = latestCongestionEvent.get();
        if (replay != null && !SseBroadcaster.sendJsonToEmitter(emitter, "traffic-congestion-update", replay)) {
            congestionEmitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcastRawEvent(TrafficEvent event) {
        latestRawEvent.set(event);
        SseBroadcaster.broadcastJson(rawEmitters, "traffic-raw", event);
    }

    public void broadcastHeatmapUpdate(HourlyTrafficDto dto) {
        latestHeatmapEvent.set(dto);
        SseBroadcaster.broadcastJson(heatmapEmitters, "traffic-heatmap-update", dto);
    }

    public void broadcastCongestionUpdate(CongestionCellDto dto) {
        latestCongestionEvent.set(dto);
        SseBroadcaster.broadcastJson(congestionEmitters, "traffic-congestion-update", dto);
    }
}
