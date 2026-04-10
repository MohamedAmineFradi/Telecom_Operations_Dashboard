package org.telecom_operations_dashboard.traffic.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TrafficRawSseBroadcaster {
    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> heatmapEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> congestionEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerRawStream() {
        return SseBroadcaster.registerEmitter(rawEmitters, "traffic-raw-connected");
    }

    public SseEmitter registerHeatmapStream() {
        return SseBroadcaster.registerEmitter(heatmapEmitters, "traffic-heatmap-connected");
    }

    public SseEmitter registerCongestionStream() {
        return SseBroadcaster.registerEmitter(congestionEmitters, "traffic-congestion-connected");
    }

    public void broadcastRawEvent(TrafficEvent event) {
        SseBroadcaster.broadcastJson(rawEmitters, "traffic-raw", event);
    }

    public void broadcastHeatmapUpdate(org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto dto) {
        SseBroadcaster.broadcastJson(heatmapEmitters, "traffic-heatmap-update", dto);
    }

    public void broadcastCongestionUpdate(org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto dto) {
        SseBroadcaster.broadcastJson(congestionEmitters, "traffic-congestion-update", dto);
    }
}
