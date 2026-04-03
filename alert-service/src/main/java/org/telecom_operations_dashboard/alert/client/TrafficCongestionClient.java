package org.telecom_operations_dashboard.alert.client;

import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface TrafficCongestionClient {

    List<CongestionCellDto> fetchCongestion(
            OffsetDateTime hour,
            int limit,
            double warningThreshold,
            double criticalThreshold
    );
}
