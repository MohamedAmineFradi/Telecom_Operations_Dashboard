package org.example.telecom_operations_dashboard.service.Impl;

import org.example.telecom_operations_dashboard.dto.CellTimeseriesPointDto;
import org.example.telecom_operations_dashboard.dto.HeatmapCellDto;
import org.example.telecom_operations_dashboard.dto.TopCellDto;
import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.model.TrafficRecord;
import org.example.telecom_operations_dashboard.repository.HourlyTrafficRepository;
import org.example.telecom_operations_dashboard.repository.TrafficRecordRepository;
import org.example.telecom_operations_dashboard.service.TrafficService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrafficServiceImpl implements TrafficService {

        private static final Logger log = LoggerFactory.getLogger(TrafficServiceImpl.class);

    private final HourlyTrafficRepository hourlyTrafficRepository;
        private final TrafficRecordRepository trafficRecordRepository;

        public TrafficServiceImpl(HourlyTrafficRepository repo, TrafficRecordRepository trafficRecordRepository) {
                this.hourlyTrafficRepository = repo;
                this.trafficRecordRepository = trafficRecordRepository;
    }

    public List<HourlyTrafficView> getTopCellsAtHour(OffsetDateTime hour, int limit) {
        return hourlyTrafficRepository.findTopCellsAtHour(hour, limit);
    }

    @Override
    public List<HeatmapCellDto> getHeatmapAt(OffsetDateTime datetime) {
        OffsetDateTime hour = datetime
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        List<HourlyTrafficView> rows = hourlyTrafficRepository.findAllAtHour(hour);

        return rows.stream()
                .map(r -> new HeatmapCellDto(
                        r.getCellId(),
                        r.getTotalActivity() != null ? r.getTotalActivity() : BigDecimal.ZERO
                ))
                .toList();
    }

    @Override
    public List<TopCellDto> getTopCells(OffsetDateTime datetime, int limit) {
        OffsetDateTime hour = datetime
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        
        List<HourlyTrafficView> topCells = hourlyTrafficRepository.findTopCellsAtHour(hour, limit);
        return topCells.stream()
                .map(r -> new TopCellDto(
                        r.getCellId(),
                        r.getTotalActivity() != null ? r.getTotalActivity() : java.math.BigDecimal.ZERO
                ))
                .toList();
    }

    @Override
    public List<CellTimeseriesPointDto> getCellTimeseries(
            Integer cellId,
            OffsetDateTime from,
            OffsetDateTime to,
            String step
    ) {
                if (from.isAfter(to)) {
                        throw new IllegalArgumentException("from must be <= to");
                }

                String normalizedStep = step == null ? "hour" : step.toLowerCase();
                List<TrafficRecord> records = trafficRecordRepository.findByCellAndInterval(cellId, from, to);
                log.info("Timeseries records fetched: cellId={}, count={}, step={}", cellId, records.size(), normalizedStep);

                Map<OffsetDateTime, Aggregate> buckets = new LinkedHashMap<>();
                for (TrafficRecord record : records) {
                        OffsetDateTime bucket = truncate(record.getDatetime(), normalizedStep);
                        Aggregate aggregate = buckets.computeIfAbsent(bucket, k -> new Aggregate());

                        BigDecimal sms = safeAdd(record.getSmsin(), record.getSmsout());
                        BigDecimal voice = safeAdd(record.getCallin(), record.getCallout());
                        BigDecimal data = safeValue(record.getInternet());

                        aggregate.sms = aggregate.sms.add(sms);
                        aggregate.voice = aggregate.voice.add(voice);
                        aggregate.data = aggregate.data.add(data);
                }

                List<CellTimeseriesPointDto> result = new ArrayList<>(buckets.size());
                for (Map.Entry<OffsetDateTime, Aggregate> entry : buckets.entrySet()) {
                        Aggregate aggregate = entry.getValue();
                        result.add(new CellTimeseriesPointDto(
                                        entry.getKey().toString(),
                                        aggregate.sms,
                                        aggregate.voice,
                                        aggregate.data
                        ));
                }
                return result;
    }

        private OffsetDateTime truncate(OffsetDateTime datetime, String step) {
                return switch (step) {
                        case "minute" -> datetime.withSecond(0).withNano(0);
                        case "day" -> datetime.toLocalDate().atStartOfDay().atOffset(datetime.getOffset());
                        case "hour" -> datetime.withMinute(0).withSecond(0).withNano(0);
                        default -> throw new IllegalArgumentException("Unsupported step: " + step);
                };
        }

        private BigDecimal safeValue(BigDecimal value) {
                return value != null ? value : BigDecimal.ZERO;
        }

        private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
                return safeValue(a).add(safeValue(b));
        }

        private static class Aggregate {
                private BigDecimal sms = BigDecimal.ZERO;
                private BigDecimal voice = BigDecimal.ZERO;
                private BigDecimal data = BigDecimal.ZERO;
        }


}
