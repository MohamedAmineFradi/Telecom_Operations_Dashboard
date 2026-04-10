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
    public List<HourlyTrafficDto> getHeatmapAtLatest(Integer limit) {
        OffsetDateTime latest = resolveLatestHour();
        if (latest == null) {
            return List.of();
        }
        return getHeatmapAtHour(latest, limit);
    }

    @Override
    public List<HourlyTrafficDto> getHeatmapAtHour(OffsetDateTime hour, Integer limit) {
        OffsetDateTime resolvedHour = hour == null ? resolveLatestHour() : NormalizationUtils.truncateToHour(hour);
        if (resolvedHour == null) {
            return List.of();
        }
        return getHeatmapInRange(resolvedHour, resolvedHour, limit);
    }

    @Override
    public List<HourlyTrafficDto> getHeatmapInRange(OffsetDateTime start, OffsetDateTime end, Integer limit) {
        if (start == null || end == null) {
            return List.of();
        }

        int safeLimit = NormalizationUtils.normalizeLimitClamped(limit, 100, MAX_LIMIT);
        Optional<ReadOnlyKeyValueStore<String, TrafficEvent>> storeOpt = getStore();
        if (storeOpt.isEmpty()) {
            return List.of();
        }

        String startPrefix = NormalizationUtils.truncateToHour(start).toString() + "|";
        String endPrefix = NormalizationUtils.truncateToHour(end).toString() + "|\uffff";

        List<HourlyTrafficDto> rows = new ArrayList<>();
        try (KeyValueIterator<String, TrafficEvent> it = storeOpt.get().range(startPrefix, endPrefix)) {
            while (it.hasNext()) {
                TrafficEvent event = it.next().value;
                if (event == null) continue;

                HourlyTrafficDto dto = trafficDtoMapper.toHourlyTrafficDto(event);
                if (dto != null) {
                    rows.add(dto);
                }
            }
        }

        rows.sort(Comparator.comparing(
                (HourlyTrafficDto row) -> row.getTotalActivity() == null ? BigDecimal.ZERO : row.getTotalActivity(),
                BigDecimal::compareTo
        ).reversed());

        if (rows.size() <= safeLimit) {
            return rows;
        }
        return rows.subList(0, safeLimit);
    }

    @Override
    public List<CongestionCellDto> getCongestionAtHour(OffsetDateTime hour, Integer limit) {
        OffsetDateTime resolvedHour = hour == null ? resolveLatestHour() : NormalizationUtils.truncateToHour(hour);
        if (resolvedHour == null) {
            return List.of();
        }
        return getCongestionInRange(resolvedHour, resolvedHour, limit);
    }

    @Override
    public List<CongestionCellDto> getCongestionAtLatest(Integer limit) {
        OffsetDateTime latest = resolveLatestHour();
        if (latest == null) {
            return List.of();
        }
        return getCongestionAtHour(latest, limit);
    }

    @Override
    public List<CongestionCellDto> getCongestionInRange(OffsetDateTime start, OffsetDateTime end, Integer limit) {
        List<HourlyTrafficDto> heatmap = getHeatmapInRange(start, end, limit);
        if (heatmap.isEmpty()) {
            return List.of();
        }

        return heatmap.stream()
                .map(row -> {
                    // Use mapper for consistency
                    TrafficEvent event = new TrafficEvent();
                    event.setCellId(row.getCellId());
                    event.setTotalActivity(row.getTotalActivity());
                    return trafficDtoMapper.toCongestionCellDto(event, congestionProperties);
                })
                .toList();
    }

    @Override
    public OffsetDateTime resolveLatestHour() {
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


}
