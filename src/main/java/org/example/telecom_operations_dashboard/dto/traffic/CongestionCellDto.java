package org.example.telecom_operations_dashboard.dto.traffic;

import java.math.BigDecimal;

public record CongestionCellDto(
        Integer cellId,
        BigDecimal totalActivity,
        double score,
        String severity
) {}
