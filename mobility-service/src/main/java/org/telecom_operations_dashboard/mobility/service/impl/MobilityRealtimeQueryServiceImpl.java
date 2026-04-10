package org.telecom_operations_dashboard.mobility.service.impl;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;
import org.telecom_operations_dashboard.mobility.dto.insight.NetworkStatsDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityCellProvinceFlowDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityProvinceSummaryDto;
import org.telecom_operations_dashboard.mobility.mapper.MobilityEventMapper;
import org.telecom_operations_dashboard.mobility.service.MobilityRealtimeQueryService;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.streaming.enabled", havingValue = "true")
public class MobilityRealtimeQueryServiceImpl implements MobilityRealtimeQueryService {

    private static final Logger log = LoggerFactory.getLogger(MobilityRealtimeQueryServiceImpl.class);
    private static final int MAX_LIMIT = 1000;
    private static final long CLEANUP_EVERY_EVENTS = 200;

    private final ConcurrentMap<FlowKey, FlowAgg> flowCache = new ConcurrentHashMap<>();
    private final AtomicLong eventCounter = new AtomicLong(0);
    private final MobilityEventMapper mobilityEventMapper;

    @Override
    public void ingestMobilityEvent(MobilityEvent event) {
        if (!mobilityEventMapper.hasRequiredFields(event)) {
            log.debug("Dropping invalid mobility event (missing required fields)");
            return;
        }

        if (!mobilityEventMapper.hasPositiveFlow(event)) {
            return;
        }

        OffsetDateTime hour = NormalizationUtils.truncateToHour(event.datetime());
        String provincia = NormalizationUtils.normalizeText(event.provincia());
        FlowKey key = new FlowKey(hour, event.cellId(), provincia);

        flowCache.compute(key, (k, agg) -> {
            BigDecimal c2p = NormalizationUtils.safe(event.cell2province());
            BigDecimal p2c = NormalizationUtils.safe(event.province2cell());
            if (agg == null) {
                return new FlowAgg(c2p, p2c);
            }
            agg.cellToProvince = agg.cellToProvince.add(c2p);
            agg.provinceToCell = agg.provinceToCell.add(p2c);
            return agg;
        });

        long count = eventCounter.incrementAndGet();
        if (count % CLEANUP_EVERY_EVENTS == 0) {
            cleanupOldBuckets(hour.minusHours(24));
        }
    }

    @Override
    public List<MobilityCellProvinceFlowDto> getRealtimeFlowsAtHour(
            OffsetDateTime hour,
            Integer cellId,
            String provincia,
            Integer limit
    ) {
        OffsetDateTime bucket = NormalizationUtils.truncateToHour(hour);
        String normalizedProvincia = NormalizationUtils.normalizeText(provincia);
        int safeLimit = NormalizationUtils.normalizeLimitClamped(limit, 100, MAX_LIMIT);

        return flowCache.entrySet().stream()
                .filter(entry -> bucket.equals(entry.getKey().hour()))
                .filter(entry -> cellId == null || cellId.equals(entry.getKey().cellId()))
                .filter(entry -> normalizedProvincia == null || normalizedProvincia.equalsIgnoreCase(entry.getKey().provincia()))
                .map(entry -> {
                    FlowKey key = entry.getKey();
                    FlowAgg agg = entry.getValue();
                    BigDecimal total = agg.cellToProvince.add(agg.provinceToCell);
                    return new MobilityCellProvinceFlowDto(
                            key.cellId(),
                            key.provincia(),
                            agg.cellToProvince,
                            agg.provinceToCell,
                            total
                    );
                })
                .sorted(Comparator.comparing(MobilityCellProvinceFlowDto::totalFlow).reversed())
                .limit(safeLimit)
                .toList();
    }

    @Override
    public List<MobilityProvinceSummaryDto> getRealtimeProvinceSummaryAtHour(
            OffsetDateTime hour,
            String provincia,
            Integer limit
    ) {
        OffsetDateTime bucket = NormalizationUtils.truncateToHour(hour);
        String normalizedProvincia = NormalizationUtils.normalizeText(provincia);
        int safeLimit = NormalizationUtils.normalizeLimitClamped(limit, 100, MAX_LIMIT);

        Map<String, BigDecimal[]> grouped = new HashMap<>();
        for (Map.Entry<FlowKey, FlowAgg> entry : flowCache.entrySet()) {
            FlowKey key = entry.getKey();
            if (!bucket.equals(key.hour())) {
                continue;
            }
            if (normalizedProvincia != null && !normalizedProvincia.equalsIgnoreCase(key.provincia())) {
                continue;
            }

            FlowAgg agg = entry.getValue();
            BigDecimal[] current = grouped.computeIfAbsent(key.provincia(), p -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            current[0] = current[0].add(agg.cellToProvince);
            current[1] = current[1].add(agg.provinceToCell);
        }

        List<MobilityProvinceSummaryDto> result = new ArrayList<>(grouped.size());
        for (Map.Entry<String, BigDecimal[]> entry : grouped.entrySet()) {
            BigDecimal c2p = entry.getValue()[0];
            BigDecimal p2c = entry.getValue()[1];
            result.add(new MobilityProvinceSummaryDto(
                    entry.getKey(),
                    c2p,
                    p2c,
                    c2p.add(p2c)
            ));
        }

        return result.stream()
                .sorted(Comparator.comparing(MobilityProvinceSummaryDto::totalFlow).reversed())
                .limit(safeLimit)
                .toList();
    }

    @Override
    public NetworkStatsDto getRealtimeNetworkStats() {
        long totalCells = flowCache.keySet().stream()
                .map(FlowKey::cellId)
                .distinct()
                .count();

        Optional<OffsetDateTime> latestHour = flowCache.keySet().stream()
                .map(FlowKey::hour)
                .max(OffsetDateTime::compareTo);

        return new NetworkStatsDto(
                0L,
                totalCells,
                eventCounter.get(),
                latestHour.map(OffsetDateTime::toString).orElse(null)
        );
    }

    private void cleanupOldBuckets(OffsetDateTime thresholdHour) {
        flowCache.keySet().removeIf(key -> key.hour().isBefore(thresholdHour));
    }

    private record FlowKey(OffsetDateTime hour, Integer cellId, String provincia) {
    }

    private static class FlowAgg {
        private BigDecimal cellToProvince;
        private BigDecimal provinceToCell;

        private FlowAgg(BigDecimal cellToProvince, BigDecimal provinceToCell) {
            this.cellToProvince = cellToProvince;
            this.provinceToCell = provinceToCell;
        }
    }
}
