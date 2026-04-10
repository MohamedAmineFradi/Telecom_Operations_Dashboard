package org.telecom_operations_dashboard.traffic.streaming.service.impl;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TrafficRawSseBroadcaster {
    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> heatmapEmitters = new CopyOnWriteArrayList<>();
    private final List<SseEmitter> congestionEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerRawStream() {
        return registerStream(rawEmitters, "traffic-raw-connected");
    }

    public SseEmitter registerHeatmapStream() {
        return registerStream(heatmapEmitters, "traffic-heatmap-connected");
    }

    public SseEmitter registerCongestionStream() {
        return registerStream(congestionEmitters, "traffic-congestion-connected");
    }

    private SseEmitter registerStream(List<SseEmitter> emitters, String connectionEventName) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        Runnable cleanup = () -> emitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name(connectionEventName)
                    .data("connected", MediaType.TEXT_PLAIN));
        } catch (IOException | IllegalStateException ex) {
            cleanup.run();
        }

        return emitter;
    }

    public void broadcastRawEvent(TrafficEvent event) {
        broadcast(rawEmitters, "traffic-raw", event);
    }

    public void broadcastHeatmapUpdate(org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto dto) {
        broadcast(heatmapEmitters, "traffic-heatmap-update", dto);
    }

    public void broadcastCongestionUpdate(org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto dto) {
        broadcast(congestionEmitters, "traffic-congestion-update", dto);
    }

    private void broadcast(List<SseEmitter> emitters, String eventName, Object data) {
        if (data == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
            }
        }
    }
}
