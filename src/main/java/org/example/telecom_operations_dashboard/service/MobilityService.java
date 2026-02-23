package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.MobilityCellProvinceFlowDto;
import org.example.telecom_operations_dashboard.dto.MobilityProvinceSummaryDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface MobilityService {
    // Intra-city cell-to-cell flows
    List<MobilityCellProvinceFlowDto> getCellFlows(
            OffsetDateTime hour,
            Integer fromCellId,
            Integer toCellId,
            Integer limit
    );

    // Inter-province flows
    List<MobilityCellProvinceFlowDto> getProvinceFlows(
            OffsetDateTime hour,
            String fromProvince,
            String toProvince,
            Integer limit
    );

    // Existing methods for backward compatibility
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
