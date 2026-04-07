package org.telecom_operations_dashboard.traffic.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
import org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto;
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

    // --- SSE Endpoints ---

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawTraffic() {
        return trafficRawSseBroadcaster.registerRawStream();
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentTraffic(
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) long intervalMs) {
        return createTrafficSseEmitter(
                Math.max(intervalMs, 1000),
                "traffic-current",
                () -> trafficRealtimeQueryService.getHeatmapAtHour(null, 1000),
                "/api/traffic/current/stream");
    }

    @GetMapping(path = "/heatmap/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamHeatmapTraffic(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) @Max(300000) long intervalMs,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        return createTrafficSseEmitter(
                intervalMs,
                "traffic-heatmap-update",
                () -> trafficRealtimeQueryService.getHeatmapAtHour(hour, limit),
                "/api/traffic/heatmap/stream");
    }

    @GetMapping(path = "/heatmap/realtime/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRealtimeHeatmapTraffic() {
        return trafficRawSseBroadcaster.registerHeatmapStream();
    }

    @GetMapping(path = "/congestion/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCongestionTraffic(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) @Max(300000) long intervalMs,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        return createTrafficSseEmitter(
                intervalMs,
                "traffic-congestion-update",
                () -> trafficRealtimeQueryService.getCongestionAtHour(hour, limit),
                "/api/traffic/congestion/stream");
    }

    @GetMapping(path = "/congestion/realtime/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRealtimeCongestionTraffic() {
        return trafficRawSseBroadcaster.registerCongestionStream();
    }

    // --- JSON Query Endpoints ---

    @GetMapping(path = "/heatmap/range", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HourlyTrafficDto> getHeatmapRange(
            @RequestParam(name = "start") String startIso,
            @RequestParam(name = "end") String endIso,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return trafficRealtimeQueryService.getHeatmapInRange(
                DateTimeParser.parse(startIso, "hour"),
                DateTimeParser.parse(endIso, "hour"),
                limit);
    }

    @GetMapping(path = "/heatmap/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HourlyTrafficDto> getHeatmapHistory(
            @RequestParam(name = "durationHours", defaultValue = "6") @Min(1) @Max(168) int durationHours,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit) {
        HistoricalRange range = resolveHistoryRange(durationHours);
        return trafficRealtimeQueryService.getHeatmapInRange(range.start(), range.end(), limit);
    }

    @GetMapping(path = "/congestion/range", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CongestionCellDto> getCongestionRange(
            @RequestParam(name = "start") String startIso,
            @RequestParam(name = "end") String endIso,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return trafficRealtimeQueryService.getCongestionInRange(
                DateTimeParser.parse(startIso, "hour"),
                DateTimeParser.parse(endIso, "hour"),
                limit);
    }

    @GetMapping(path = "/congestion/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CongestionCellDto> getCongestionHistory(
            @RequestParam(name = "durationHours", defaultValue = "6") @Min(1) @Max(168) int durationHours,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(1000) int limit) {
        HistoricalRange range = resolveHistoryRange(durationHours);
        return trafficRealtimeQueryService.getCongestionInRange(range.start(), range.end(), limit);
    }

    // --- Helper Methods ---

    private HistoricalRange resolveHistoryRange(int durationHours) {
        OffsetDateTime end = resolveHour(null);
        OffsetDateTime start = end.minusHours(durationHours);
        return new HistoricalRange(start, end);
    }

    private record HistoricalRange(OffsetDateTime start, OffsetDateTime end) {
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
            String endpoint) {
        long safeInterval = Math.max(intervalMs, 1000);
        long sseTimeoutMs = Math.max(safeInterval * 3, 15_000L);

        return createScheduledEmitter(
                safeInterval,
                sseTimeoutMs,
                eventName,
                endpoint,
                dataSupplier::get,
                true);
    }
}
