package org.telecom_operations_dashboard.mobility.streaming.service;

import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;
import org.telecom_operations_dashboard.mobility.dto.insight.NetworkStatsDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityCellProvinceFlowDto;
import org.telecom_operations_dashboard.mobility.dto.mobility.MobilityProvinceSummaryDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface MobilityRealtimeQueryService {

        void ingestMobilityEvent(MobilityEvent event);

    List<MobilityCellProvinceFlowDto> getRealtimeFlowsAtHour(
            OffsetDateTime hour,
            Integer cellId,
            String provincia,
            Integer limit
    );

    List<MobilityProvinceSummaryDto> getRealtimeProvinceSummaryAtHour(
            OffsetDateTime hour,
            String provincia,
            Integer limit
    );

    NetworkStatsDto getRealtimeNetworkStats();
}
