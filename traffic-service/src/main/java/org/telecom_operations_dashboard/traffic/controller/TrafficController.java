package org.telecom_operations_dashboard.traffic.controller;

import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.util.DateTimeParser;
import org.telecom_operations_dashboard.traffic.streaming.service.TrafficRealtimeQueryService;
import org.telecom_operations_dashboard.traffic.streaming.service.impl.TrafficRawSseBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
@Validated
@ConditionalOnProperty(name = "app.service", havingValue = "traffic")
public class TrafficController {

    private static final Logger log = LoggerFactory.getLogger(TrafficController.class);

    private final TrafficRealtimeQueryService trafficRealtimeQueryService;
    private final TrafficRawSseBroadcaster trafficRawSseBroadcaster;
    private final ScheduledExecutorService sseScheduler = Executors.newScheduledThreadPool(2);

    @PreDestroy
    void shutdownSseScheduler() {
        sseScheduler.shutdownNow();
    }

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
        log.info("Traffic SSE stream opened endpoint={} event={} intervalMs={}", endpoint, eventName, intervalMs);

        long safeInterval = Math.max(intervalMs, 1000);
        long sseTimeoutMs = Math.max(safeInterval * 3, 15_000L);
        SseEmitter emitter = new SseEmitter(sseTimeoutMs);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> taskRef = new AtomicReference<>();

        Runnable shutdown = () -> {
            if (closed.compareAndSet(false, true)) {
                ScheduledFuture<?> task = taskRef.get();
                if (task != null) {
                    task.cancel(true);
                }
                log.info("Traffic SSE stream closed endpoint={} (timeout={}ms)", endpoint, sseTimeoutMs);
            }
        };

        emitter.onCompletion(shutdown);
        emitter.onTimeout(() -> {
            shutdown.run();
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // no-op
            }
        });
        emitter.onError(error -> shutdown.run());

        ScheduledFuture<?> task = sseScheduler.scheduleAtFixedRate(() -> {
            if (closed.get()) {
                return;
            }

            try {
                List<?> payload = dataSupplier.get();
                if (!payload.isEmpty()) {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .id(String.valueOf(System.currentTimeMillis()))
                            .data(payload));
                }
            } catch (IOException e) {
                log.debug("Traffic SSE client disconnected for {}", endpoint);
                shutdown.run();
            } catch (IllegalStateException e) {
                shutdown.run();
            } catch (Exception e) {
                log.error("Failed to push SSE update for {}", endpoint, e);
                shutdown.run();
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // no-op
                }
            }
        }, 0, safeInterval, TimeUnit.MILLISECONDS);
        taskRef.set(task);

        return emitter;
    }
}

