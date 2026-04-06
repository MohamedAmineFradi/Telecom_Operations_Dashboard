package org.telecom_operations_dashboard.traffic.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.controller.AbstractSseController;
import org.telecom_operations_dashboard.common.util.DateTimeParser;
import org.telecom_operations_dashboard.traffic.streaming.service.TrafficRealtimeQueryService;
import org.telecom_operations_dashboard.traffic.streaming.service.impl.TrafficRawSseBroadcaster;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
@Validated
@ConditionalOnProperty(name = "app.service", havingValue = "traffic")
public class TrafficController extends AbstractSseController {

    private final TrafficRealtimeQueryService trafficRealtimeQueryService;
    private final TrafficRawSseBroadcaster trafficRawSseBroadcaster;

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawTraffic() {
        return trafficRawSseBroadcaster.registerRawStream();
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentTraffic(
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) long intervalMs
    ) {
        return createTrafficSseEmitter(
                Math.max(intervalMs, 1000),
                "traffic-current",
                () -> trafficRealtimeQueryService.getHeatmapAtHour(null, 1000),
                "/api/traffic/current/stream"
        );
    }

    @GetMapping(path = "/heatmap/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamHeatmapTraffic(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) @Max(300000) long intervalMs,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit
    ) {
        OffsetDateTime hour = resolveHour(hourIso);
        return createTrafficSseEmitter(
                intervalMs,
                "traffic-heatmap-update",
                () -> trafficRealtimeQueryService.getHeatmapAtHour(hour, limit),
                "/api/traffic/heatmap/stream"
        );
    }

    @GetMapping(path = "/congestion/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCongestionTraffic(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) @Max(300000) long intervalMs,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit
    ) {
        OffsetDateTime hour = resolveHour(hourIso);
        return createTrafficSseEmitter(
                intervalMs,
                "traffic-congestion-update",
                () -> trafficRealtimeQueryService.getCongestionAtHour(hour, limit),
                "/api/traffic/congestion/stream"
        );
    }

    private OffsetDateTime resolveHour(String hourIso) {
        if (hourIso == null || hourIso.isBlank()) {
            OffsetDateTime latest = trafficRealtimeQueryService.resolveHourOrLatest(null);
            return latest != null ? latest : OffsetDateTime.now(ZoneOffset.UTC).withMinute(0).withSecond(0).withNano(0);
        }
        return DateTimeParser.parse(hourIso, "hour");
    }

    private SseEmitter createTrafficSseEmitter(
            long intervalMs,
            String eventName,
            Supplier<List<?>> dataSupplier,
            String endpoint
    ) {
        long safeInterval = Math.max(intervalMs, 1000);
        long sseTimeoutMs = Math.max(safeInterval * 3, 15_000L);
        
        return createScheduledEmitter(
                safeInterval,
                sseTimeoutMs,
                eventName,
                endpoint,
                dataSupplier::get,
                true
        );
    }
}

