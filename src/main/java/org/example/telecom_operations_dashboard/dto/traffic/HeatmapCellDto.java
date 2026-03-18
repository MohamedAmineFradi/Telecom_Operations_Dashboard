package org.example.telecom_operations_dashboard.dto.traffic;

import java.math.BigDecimal;

public record HeatmapCellDto(
        Integer cellId,
        BigDecimal totalSmsin,
        BigDecimal totalSmsout,
        BigDecimal totalCallin,
        BigDecimal totalCallout,
        BigDecimal totalInternet,
        BigDecimal totalActivity,
        String bounds,
        Double lon,
        Double lat
) {}
