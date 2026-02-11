package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.CellTimeseriesPointDto;
import org.example.telecom_operations_dashboard.dto.HeatmapCellDto;
import org.example.telecom_operations_dashboard.dto.HourlyCellDto;
import org.example.telecom_operations_dashboard.dto.HourlyTrafficSummaryDto;
import org.example.telecom_operations_dashboard.dto.TopCellDto;
import org.example.telecom_operations_dashboard.model.HourlyTrafficView;

import java.time.OffsetDateTime;
import java.util.List;

public interface TrafficService {
    public List<HourlyTrafficView> getTopCellsAtHour(OffsetDateTime hour, int limit);
    List<HourlyCellDto> getAllCellsAtHour(OffsetDateTime hour);
    HourlyTrafficSummaryDto getHourlySummaryAtHour(OffsetDateTime hour);
    List<HeatmapCellDto> getHeatmapAt(OffsetDateTime datetime);

    List<TopCellDto> getTopCells(OffsetDateTime datetime, int limit);

    List<CellTimeseriesPointDto> getCellTimeseries(
            Integer cellId,
            OffsetDateTime from,
            OffsetDateTime to,
            String step
    );
}
