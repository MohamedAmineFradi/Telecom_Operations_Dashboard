package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.traffic.CellTimeseriesPointDto;
import org.example.telecom_operations_dashboard.dto.traffic.CongestionCellDto;
import org.example.telecom_operations_dashboard.dto.traffic.HeatmapCellDto;
import org.example.telecom_operations_dashboard.dto.traffic.HourlyCellDto;
import org.example.telecom_operations_dashboard.dto.traffic.HourlyTrafficSummaryDto;
import org.example.telecom_operations_dashboard.dto.traffic.TopCellDto;
import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.springframework.cache.annotation.Cacheable;

import java.time.OffsetDateTime;
import java.util.List;

public interface TrafficService {
    public List<HourlyTrafficView> getTopCellsAtHour(OffsetDateTime hour, int limit);
    List<HourlyCellDto> getAllCellsAtHour(OffsetDateTime hour);
    HourlyTrafficSummaryDto getHourlySummaryAtHour(OffsetDateTime hour);
    
    @Cacheable(value = "heatmap", key = "#datetime.withMinute(0).withSecond(0).withNano(0)")
    List<HeatmapCellDto> getHeatmapAt(OffsetDateTime datetime);

    List<TopCellDto> getTopCells(OffsetDateTime datetime, int limit);

    List<CellTimeseriesPointDto> getCellTimeseries(
            Integer cellId,
            OffsetDateTime from,
            OffsetDateTime to,
            String step
    );

        List<CongestionCellDto> getCongestionAtHour(
            OffsetDateTime hour,
            int limit,
            double warningThreshold,
            double criticalThreshold
        );
}
