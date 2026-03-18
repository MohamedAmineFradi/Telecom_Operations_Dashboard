package org.example.telecom_operations_dashboard.dto.mobility;

import java.math.BigDecimal;

public record MobilityFlowDto(
        Integer fromCellId,
        Integer toCellId,
        BigDecimal volume
) {}
