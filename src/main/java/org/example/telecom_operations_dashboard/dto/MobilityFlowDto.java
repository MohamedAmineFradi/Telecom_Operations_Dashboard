package org.example.telecom_operations_dashboard.dto;

import java.math.BigDecimal;

public record MobilityFlowDto(
        Integer fromCellId,
        Integer toCellId,
        BigDecimal volume
) {}
