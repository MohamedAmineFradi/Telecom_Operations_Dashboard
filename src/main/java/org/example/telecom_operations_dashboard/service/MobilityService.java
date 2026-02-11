package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.MobilityCellProvinceFlowDto;
import org.example.telecom_operations_dashboard.dto.MobilityProvinceSummaryDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface MobilityService {
    List<MobilityCellProvinceFlowDto> getMobilityFlowsAtHour(
            OffsetDateTime hour,
            Integer cellId,
            String provincia,
            Integer limit
    );
    List<MobilityProvinceSummaryDto> getProvinceSummariesAtHour(
            OffsetDateTime hour,
            String provincia,
            Integer limit
    );
}
