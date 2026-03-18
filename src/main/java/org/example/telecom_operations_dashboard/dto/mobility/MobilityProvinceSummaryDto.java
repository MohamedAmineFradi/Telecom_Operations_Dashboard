package org.example.telecom_operations_dashboard.dto.mobility;

import java.math.BigDecimal;

public record MobilityProvinceSummaryDto(
        String provincia,
        BigDecimal totalCellToProvince,
        BigDecimal totalProvinceToCell,
        BigDecimal totalFlow
) {}
