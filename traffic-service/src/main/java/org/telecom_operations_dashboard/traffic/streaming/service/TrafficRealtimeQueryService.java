package org.telecom_operations_dashboard.traffic.streaming.service;

import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
import org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface TrafficRealtimeQueryService {

    List<HourlyTrafficDto> getHeatmapAtHour(OffsetDateTime hour, Integer limit);

    List<HourlyTrafficDto> getHeatmapInRange(OffsetDateTime start, OffsetDateTime end, Integer limit);

    List<CongestionCellDto> getCongestionAtHour(OffsetDateTime hour, Integer limit);

    List<CongestionCellDto> getCongestionInRange(OffsetDateTime start, OffsetDateTime end, Integer limit);

    OffsetDateTime resolveHourOrLatest(OffsetDateTime requestedHour);
}
