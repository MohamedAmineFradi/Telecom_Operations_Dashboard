package org.example.telecom_operations_dashboard.service.Impl;

import org.example.telecom_operations_dashboard.dto.CellTimeseriesPointDto;
import org.example.telecom_operations_dashboard.dto.CongestionCellDto;
import org.example.telecom_operations_dashboard.dto.HeatmapCellDto;
import org.example.telecom_operations_dashboard.dto.HourlyCellDto;
import org.example.telecom_operations_dashboard.dto.HourlyTrafficSummaryDto;
import org.example.telecom_operations_dashboard.dto.TopCellDto;
import org.example.telecom_operations_dashboard.model.HeatmapCellView;
import org.example.telecom_operations_dashboard.model.HourlyTrafficSummaryView;
import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.model.TrafficRecord;
import org.example.telecom_operations_dashboard.repository.HourlyTrafficRepository;
import org.example.telecom_operations_dashboard.repository.TrafficRecordRepository;
import org.example.telecom_operations_dashboard.service.TrafficService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

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
        public List<HourlyCellDto> getAllCellsAtHour(OffsetDateTime hour) {
                return hourlyTrafficRepository.findAllAtHour(hour).stream()
                                .map(r -> new HourlyCellDto(
                                                r.getCellId(),
                                                safeValue(r.getTotalSmsin()),
                                                safeValue(r.getTotalSmsout()),
                                                safeValue(r.getTotalCallin()),
                                                safeValue(r.getTotalCallout()),
                                                safeValue(r.getTotalInternet()),
                                                safeValue(r.getTotalActivity())
                                ))
                                .toList();
        }

        @Override
        public HourlyTrafficSummaryDto getHourlySummaryAtHour(OffsetDateTime hour) {
                HourlyTrafficSummaryView summary = hourlyTrafficRepository.findSummaryAtHour(hour);
                if (summary == null) {
                        return new HourlyTrafficSummaryDto(
                                        hour.toString(),
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO
                        );
                }
                return new HourlyTrafficSummaryDto(
                                summary.getHour() != null ? summary.getHour().toString() : hour.toString(),
                                safeValue(summary.getTotalSmsin()),
                                safeValue(summary.getTotalSmsout()),
                                safeValue(summary.getTotalCallin()),
                                safeValue(summary.getTotalCallout()),
                                safeValue(summary.getTotalInternet()),
                                safeValue(summary.getTotalActivity())
                );
        }

    @Override
    public List<HeatmapCellDto> getHeatmapAt(OffsetDateTime datetime) {
        OffsetDateTime hour = datetime
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        List<HeatmapCellView> rows = hourlyTrafficRepository.findHeatmapWithGeometry(hour);

        return rows.stream()
                .map(r -> new HeatmapCellDto(
                        r.getCellId(),
                        r.getTotalSmsin() != null ? r.getTotalSmsin() : BigDecimal.ZERO,
                        r.getTotalSmsout() != null ? r.getTotalSmsout() : BigDecimal.ZERO,
                        r.getTotalCallin() != null ? r.getTotalCallin() : BigDecimal.ZERO,
                        r.getTotalCallout() != null ? r.getTotalCallout() : BigDecimal.ZERO,
                        r.getTotalInternet() != null ? r.getTotalInternet() : BigDecimal.ZERO,
                        r.getTotalActivity() != null ? r.getTotalActivity() : BigDecimal.ZERO,
                        r.getBounds(),
                        r.getLon(),
                        r.getLat()
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
                        r.getTotalSmsin() != null ? r.getTotalSmsin() : java.math.BigDecimal.ZERO,
                        r.getTotalSmsout() != null ? r.getTotalSmsout() : java.math.BigDecimal.ZERO,
                        r.getTotalCallin() != null ? r.getTotalCallin() : java.math.BigDecimal.ZERO,
                        r.getTotalCallout() != null ? r.getTotalCallout() : java.math.BigDecimal.ZERO,
                        r.getTotalInternet() != null ? r.getTotalInternet() : java.math.BigDecimal.ZERO,
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

    @Override
    public List<CongestionCellDto> getCongestionAtHour(
            OffsetDateTime hour,
            int limit,
            double warningThreshold,
            double criticalThreshold
    ) {
        validateThresholds(warningThreshold, criticalThreshold);
        OffsetDateTime bucket = truncate(hour, "hour");
        List<HourlyTrafficView> rows = hourlyTrafficRepository.findAllAtHour(bucket);
        if (rows.isEmpty()) {
            return List.of();
        }

        BigDecimal maxActivity = rows.stream()
                .map(r -> safeValue(r.getTotalActivity()))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (maxActivity.compareTo(BigDecimal.ZERO) <= 0) {
            return rows.stream()
                    .map(r -> new CongestionCellDto(
                            r.getCellId(),
                            safeValue(r.getTotalActivity()),
                            0.0,
                            "NORMAL"
                    ))
                    .toList();
        }

        return rows.stream()
                .map(r -> {
                    BigDecimal activity = safeValue(r.getTotalActivity());
                    double score = activity.divide(maxActivity, 6, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    String severity = severityFor(score, warningThreshold, criticalThreshold);
                    return new CongestionCellDto(r.getCellId(), activity, score, severity);
                })
                .sorted(Comparator.comparingDouble(CongestionCellDto::score).reversed())
                .limit(Math.max(1, limit))
                .toList();
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

                private void validateThresholds(double warningThreshold, double criticalThreshold) {
                        if (warningThreshold < 0 || warningThreshold > 100 || criticalThreshold < 0 || criticalThreshold > 100) {
                                throw new IllegalArgumentException("thresholds must be between 0 and 100");
                        }
                        if (warningThreshold >= criticalThreshold) {
                                throw new IllegalArgumentException("warningThreshold must be < criticalThreshold");
                        }
                }

                private String severityFor(double score, double warningThreshold, double criticalThreshold) {
                        if (score >= criticalThreshold) {
                                return "CRITICAL";
                        }
                        if (score >= warningThreshold) {
                                return "WARNING";
                        }
                        return "NORMAL";
                }


}
