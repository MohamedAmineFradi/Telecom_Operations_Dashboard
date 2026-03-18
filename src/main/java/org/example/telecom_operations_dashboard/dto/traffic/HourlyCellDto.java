package org.example.telecom_operations_dashboard.dto.traffic;

import java.math.BigDecimal;

public record HourlyCellDto(
        Integer cellId,
        BigDecimal totalSmsin,
        BigDecimal totalSmsout,
        BigDecimal totalCallin,
        BigDecimal totalCallout,
        BigDecimal totalInternet,
        BigDecimal totalActivity
) {}
