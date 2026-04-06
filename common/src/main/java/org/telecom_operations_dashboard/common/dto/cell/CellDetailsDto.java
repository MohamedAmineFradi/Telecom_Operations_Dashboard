package org.telecom_operations_dashboard.common.dto.cell;

import java.math.BigDecimal;

public record CellDetailsDto(
        Integer cellId,
        Integer countryCode,
        BigDecimal smsIn,
        BigDecimal smsOut,
        BigDecimal callIn,
        BigDecimal callOut,
        BigDecimal internet,
        BigDecimal totalActivity,
        String bounds,
        Double centroidX,
        Double centroidY
) {}
