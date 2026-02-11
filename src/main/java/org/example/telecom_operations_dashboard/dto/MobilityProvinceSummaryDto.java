package org.example.telecom_operations_dashboard.dto;

import java.math.BigDecimal;

public record MobilityProvinceSummaryDto(
        String provincia,
        BigDecimal totalCellToProvince,
        BigDecimal totalProvinceToCell,
        BigDecimal totalFlow
) {}
