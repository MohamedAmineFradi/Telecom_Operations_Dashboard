package org.telecom_operations_dashboard.common.dto.traffic;

import java.math.BigDecimal;

public record CongestionCellDto(
        Integer cellId,
        BigDecimal totalActivity,
        double score,
        String severity
) {}
