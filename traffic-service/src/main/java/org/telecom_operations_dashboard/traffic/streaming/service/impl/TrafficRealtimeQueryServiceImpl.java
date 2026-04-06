package org.telecom_operations_dashboard.traffic.streaming.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
import org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.telecom_operations_dashboard.traffic.mapper.TrafficDtoMapper;
import org.telecom_operations_dashboard.traffic.streaming.config.TrafficCongestionProperties;
import org.telecom_operations_dashboard.traffic.streaming.config.TrafficStreamsStoreNames;
import org.telecom_operations_dashboard.traffic.streaming.service.TrafficRealtimeQueryService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.streaming.enabled", havingValue = "true")
public class TrafficRealtimeQueryServiceImpl implements TrafficRealtimeQueryService {

    private static final Logger log = LoggerFactory.getLogger(TrafficRealtimeQueryServiceImpl.class);
    private static final int MAX_LIMIT = 1000;

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private final TrafficDtoMapper trafficDtoMapper;
    private final TrafficCongestionProperties congestionProperties;

    @Override
    public List<HourlyTrafficDto> getHeatmapAtHour(OffsetDateTime hour, Integer limit) {
        OffsetDateTime resolvedHour = resolveHourOrLatest(hour);
        if (resolvedHour == null) {
            return List.of();
        }

        int safeLimit = NormalizationUtils.normalizeLimitClamped(limit, 100, MAX_LIMIT);
        Optional<ReadOnlyKeyValueStore<String, TrafficEvent>> storeOpt = getStore();
        if (storeOpt.isEmpty()) {
            return List.of();
        }

        List<HourlyTrafficDto> rows = new ArrayList<>();
        try (KeyValueIterator<String, TrafficEvent> all = storeOpt.get().all()) {
            while (all.hasNext()) {
                TrafficEvent event = all.next().value;
                if (event == null || event.getHour() == null || !resolvedHour.equals(event.getHour())) {
                    continue;
                }

                HourlyTrafficDto dto = trafficDtoMapper.toHourlyTrafficDto(event);
                if (dto != null) {
                    rows.add(dto);
                }
            }
        }

        rows.sort(Comparator.comparing(
            (HourlyTrafficDto row) -> safe(row.getTotalActivity()),
            BigDecimal::compareTo
        ).reversed());
        if (rows.size() <= safeLimit) {
            return rows;
        }
        return rows.subList(0, safeLimit);
    }

    @Override
    public List<CongestionCellDto> getCongestionAtHour(OffsetDateTime hour, Integer limit) {
        List<HourlyTrafficDto> heatmap = getHeatmapAtHour(hour, limit);
        if (heatmap.isEmpty()) {
            return List.of();
        }

        BigDecimal lowMax = congestionProperties.normalizedLowMax();
        BigDecimal mediumMax = congestionProperties.normalizedMediumMax();

        return heatmap.stream()
                .map(row -> {
                    BigDecimal total = safe(row.getTotalActivity());
                    String severity;
                    double score;

                    if (total.compareTo(lowMax) <= 0) {
                        severity = "LOW";
                        score = 33.0;
                    } else if (total.compareTo(mediumMax) <= 0) {
                        severity = "MEDIUM";
                        score = 66.0;
                    } else {
                        severity = "HIGH";
                        score = 100.0;
                    }

                    return new CongestionCellDto(row.getCellId(), total, score, severity);
                })
                .toList();
    }

    @Override
    public OffsetDateTime resolveHourOrLatest(OffsetDateTime requestedHour) {
        if (requestedHour != null) {
            return NormalizationUtils.truncateToHour(requestedHour);
        }

        Optional<ReadOnlyKeyValueStore<String, TrafficEvent>> storeOpt = getStore();
        if (storeOpt.isEmpty()) {
            return null;
        }

        OffsetDateTime latest = null;
        try (KeyValueIterator<String, TrafficEvent> all = storeOpt.get().all()) {
            while (all.hasNext()) {
                TrafficEvent event = all.next().value;
                if (event == null || event.getHour() == null) {
                    continue;
                }
                if (latest == null || event.getHour().isAfter(latest)) {
                    latest = event.getHour();
                }
            }
        }
        return latest;
    }

    private Optional<ReadOnlyKeyValueStore<String, TrafficEvent>> getStore() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(kafkaStreams.store(
                    StoreQueryParameters.fromNameAndType(
                            TrafficStreamsStoreNames.TRAFFIC_HOURLY_CELL_STORE,
                            QueryableStoreTypes.keyValueStore()
                    )
            ));
        } catch (InvalidStateStoreException ex) {
            log.debug("Traffic Streams store not ready yet: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
