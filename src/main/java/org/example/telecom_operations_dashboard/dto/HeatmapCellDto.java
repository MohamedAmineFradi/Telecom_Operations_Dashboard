package org.example.telecom_operations_dashboard.dto;

import java.math.BigDecimal;

public record HeatmapCellDto(
        Integer cellId,
        BigDecimal totalActivity
) {}
