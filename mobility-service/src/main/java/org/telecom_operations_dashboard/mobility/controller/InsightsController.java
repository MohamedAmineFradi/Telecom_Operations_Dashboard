package org.telecom_operations_dashboard.mobility.controller;

import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.util.DateTimeParser;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.telecom_operations_dashboard.mobility.dto.insight.NetworkStatsDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityCellProvinceFlowDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityProvinceSummaryDto;
import org.telecom_operations_dashboard.mobility.streaming.service.impl.MobilityRawSseBroadcaster;
import org.telecom_operations_dashboard.mobility.streaming.service.MobilityRealtimeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.service", havingValue = "mobility")
public class InsightsController {

    private static final Logger log = LoggerFactory.getLogger(InsightsController.class);

    private final MobilityRealtimeQueryService mobilityRealtimeQueryService;
    private final MobilityRawSseBroadcaster mobilityRawSseBroadcaster;
    private final ScheduledExecutorService sseScheduler = Executors.newScheduledThreadPool(4, r -> {
        Thread t = new Thread(r, "SSE-Mobility-Shared-" + System.nanoTime());
        t.setDaemon(false);
        return t;
    });

    @PreDestroy
    void shutdownSseScheduler() {
        sseScheduler.shutdownNow();
    }

    @GetMapping(path = "/mobility/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawMobility() {
        return mobilityRawSseBroadcaster.registerRawStream();
    }

    // Cell-scoped flows
    @GetMapping("/mobility/cell-flows")
    public ResponseEntity<List<MobilityCellProvinceFlowDto>> getCellFlows(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "fromCell", required = false) Integer fromCellId,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        log.info("Cell flows requested at {} (fromCell={}, limit={})", hourIso, fromCellId, limit);
        return ResponseEntity.ok(mobilityRealtimeQueryService.getRealtimeFlowsAtHour(hour, fromCellId, null, limit));
    }

    // Province-scoped flows
    @GetMapping("/mobility/province-flows")
    public ResponseEntity<List<MobilityCellProvinceFlowDto>> getProvinceFlows(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "from", required = false) String fromProvince,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        log.info("Province flows requested at {} (from={}, limit={})", hourIso, fromProvince, limit);
        return ResponseEntity.ok(mobilityRealtimeQueryService.getRealtimeFlowsAtHour(hour, null, fromProvince, limit));
    }

    @GetMapping("/mobility/province-summary")
    public ResponseEntity<List<MobilityProvinceSummaryDto>> getProvinceSummaries(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "provincia", required = false) String provincia,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        log.info("Province summaries requested at {} (provincia={}, limit={})", hourIso, provincia, limit);
        return ResponseEntity.ok(mobilityRealtimeQueryService.getRealtimeProvinceSummaryAtHour(hour, provincia, limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<NetworkStatsDto> getNetworkStats() {
        log.info("Network stats requested (realtime mobility cache)");
        return ResponseEntity.ok(mobilityRealtimeQueryService.getRealtimeNetworkStats());
    }


        @GetMapping(
            path = {
                "/mobility/stream",
                "/mobility/current/stream",
                "/mobility/flows/stream",
                "/mobility/cell-flows/stream",
                "/mobility/province-flows/stream",
                "/mobility/province-summary/stream"
            },
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
        )
    public SseEmitter streamMobilityFlows(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "view", defaultValue = "flows") String view,
            @RequestParam(name = "fromCell", required = false) Integer fromCell,
            @RequestParam(name = "from", required = false) String fromProvince,
            @RequestParam(name = "provincia", required = false) String provincia,
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) @Max(300000) long intervalMs,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        OffsetDateTime hour = resolveHour(hourIso);
        boolean summaryView = "summary".equalsIgnoreCase(view) || "province-summary".equalsIgnoreCase(view);
        String endpoint = summaryView ? "/api/mobility/province-summary/stream" : "/api/mobility/stream";
        String eventName = summaryView ? "mobility-province-summary-update" : "mobility-flows-update";

        String provinceFilter = summaryView ? provincia : (fromProvince != null ? fromProvince : provincia);

        return createMobilitySseEmitter(
                intervalMs,
            eventName,
            () -> summaryView
                ? realtimeProvinceSummaries(hour, provinceFilter, limit)
                : realtimeMobilityFlows(hour, fromCell, provinceFilter, limit),
            endpoint
        );
    }

    private List<MobilityCellProvinceFlowDto> realtimeMobilityFlows(
            OffsetDateTime hour,
            Integer cellId,
            String provincia,
            int limit
    ) {
        return mobilityRealtimeQueryService.getRealtimeFlowsAtHour(hour, cellId, provincia, limit);
    }

    private List<MobilityProvinceSummaryDto> realtimeProvinceSummaries(
            OffsetDateTime hour,
            String provincia,
            int limit
    ) {
        return mobilityRealtimeQueryService.getRealtimeProvinceSummaryAtHour(hour, provincia, limit);
    }

    private OffsetDateTime resolveHour(String hourIso) {
        if (hourIso == null || hourIso.isBlank()) {
            String latestTrafficAt = mobilityRealtimeQueryService.getRealtimeNetworkStats().latestTrafficAt();
            if (latestTrafficAt != null && !latestTrafficAt.isBlank()) {
                try {
                    return NormalizationUtils.truncateToHour(OffsetDateTime.parse(latestTrafficAt));
                } catch (Exception ignored) {
                    // fallback below
                }
            }
            return NormalizationUtils.truncateToHour(OffsetDateTime.now(ZoneOffset.UTC));
        }
        return DateTimeParser.parse(hourIso, "hour");
    }


        private SseEmitter createMobilitySseEmitter(
            long intervalMs,
            String eventName,
            Supplier<List<?>> dataSupplier,
            String endpoint
    ) {
        log.info("Mobility SSE stream opened endpoint={} event={} intervalMs={}", endpoint, eventName, intervalMs);

        long sseTimeoutMs = Math.max(intervalMs * 3, 15 * 1000L); // At least 3 intervals or 15s
        SseEmitter emitter = new SseEmitter(sseTimeoutMs);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> taskRef = new AtomicReference<>();

        Runnable shutdown = () -> {
            if (closed.compareAndSet(false, true)) {
                ScheduledFuture<?> task = taskRef.get();
                if (task != null) {
                    task.cancel(true);
                }
                log.info("Mobility SSE stream closed endpoint={} (timeout={}ms)", endpoint, sseTimeoutMs);
            }
        };

        emitter.onCompletion(shutdown);
        emitter.onTimeout(() -> {
            log.warn("SSE timeout for {} after {}ms", endpoint, sseTimeoutMs);
            shutdown.run();
            try {
                emitter.complete();
            } catch (Exception ex) {
                log.debug("Error completing emitter on timeout", ex);
            }
        });
        emitter.onError(error -> {
            log.debug("SSE error for {}: {}", endpoint, error.getMessage());
            shutdown.run();
        });

        ScheduledFuture<?> task = sseScheduler.scheduleAtFixedRate(() -> {
            if (closed.get()) {
                return;
            }

            try {
                List<?> payload = dataSupplier.get();
                if (!payload.isEmpty()) {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(payload)
                            .id(String.valueOf(System.currentTimeMillis())));
                    log.debug("Sent {} mobility updates to {}", payload.size(), endpoint);
                }
            } catch (IOException e) {
                log.debug("SSE client disconnected from {} (IOException)", endpoint);
                shutdown.run();
            } catch (IllegalStateException e) {
                log.debug("Emitter closed for {}", endpoint);
                shutdown.run();
            } catch (Exception e) {
                log.error("Failed to emit mobility update for {}", endpoint, e);
                try {
                    shutdown.run();
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.debug("Error during error completion", ex);
                }
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
        taskRef.set(task);

        return emitter;
    }
}
