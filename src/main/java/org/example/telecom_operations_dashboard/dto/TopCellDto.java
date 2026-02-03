package org.example.telecom_operations_dashboard.dto;

import java.math.BigDecimal;

public record TopCellDto(
        Integer cellId,
        BigDecimal totalActivity
) {}
