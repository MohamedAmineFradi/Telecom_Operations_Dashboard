package org.telecom_operations_dashboard.mobility.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.controller.AbstractSseController;
import org.telecom_operations_dashboard.common.util.DateTimeParser;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.telecom_operations_dashboard.mobility.dto.insight.NetworkStatsDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityCellProvinceFlowDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityProvinceSummaryDto;
import org.telecom_operations_dashboard.mobility.service.MobilityRealtimeQueryService;
import org.telecom_operations_dashboard.mobility.service.impl.MobilityRawSseBroadcaster;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/mobility")
@RequiredArgsConstructor
@Validated
@ConditionalOnProperty(name = "app.service", havingValue = "mobility")
public class MobilityController extends AbstractSseController {

    private final MobilityRealtimeQueryService mobilityRealtimeQueryService;
    private final MobilityRawSseBroadcaster mobilityRawSseBroadcaster;

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawMobility() {
        return mobilityRawSseBroadcaster.registerRawStream();
    }

    // Cell-scoped flows
    @GetMapping({"/mobility/cell-flows", "/cell-flows"})
    public ResponseEntity<List<MobilityCellProvinceFlowDto>> getMobilityCellFlows(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "fromCell", required = false) Integer fromCellId,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        log.info("Cell flows requested at {} (fromCell={}, limit={})", hourIso, fromCellId, limit);
        return ResponseEntity.ok(mobilityRealtimeQueryService.getRealtimeFlowsAtHour(hour, fromCellId, null, limit));
    }

    // Province-scoped flows
        @GetMapping("/province-flows")
        public ResponseEntity<List<MobilityCellProvinceFlowDto>> getMobilityProvinceFlows(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "from", required = false) String fromProvince,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        log.info("Province flows requested at {} (from={}, limit={})", hourIso, fromProvince, limit);
        return ResponseEntity.ok(mobilityRealtimeQueryService.getRealtimeFlowsAtHour(hour, null, fromProvince, limit));
    }

        @GetMapping("/province-summary")
        public ResponseEntity<List<MobilityProvinceSummaryDto>> getMobilityProvinceSummaries(
            @RequestParam(name = "hour", required = false) String hourIso,
            @RequestParam(name = "provincia", required = false) String provincia,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = resolveHour(hourIso);
        log.info("Province summaries requested at {} (provincia={}, limit={})", hourIso, provincia, limit);
        return ResponseEntity.ok(mobilityRealtimeQueryService.getRealtimeProvinceSummaryAtHour(hour, provincia, limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<NetworkStatsDto> getMobilityNetworkStats() {
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
    public SseEmitter streamCurrentMobility(
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

        String provinceFilter = summaryView ? provincia : fromProvince != null ? fromProvince : provincia;

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
        return DateTimeParser.parseIfPresent(hourIso, "hour").orElseThrow();
    }

    private SseEmitter createMobilitySseEmitter(
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
