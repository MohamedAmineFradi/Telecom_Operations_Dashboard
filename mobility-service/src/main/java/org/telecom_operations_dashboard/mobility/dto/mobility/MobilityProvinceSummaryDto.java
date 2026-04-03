package org.telecom_operations_dashboard.mobility.dto.mobility;

import java.math.BigDecimal;

public record MobilityProvinceSummaryDto(
        String provincia,
        BigDecimal totalCellToProvince,
        BigDecimal totalProvinceToCell,
        BigDecimal totalFlow
) {}
