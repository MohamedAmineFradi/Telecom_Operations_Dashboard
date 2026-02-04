package org.example.telecom_operations_dashboard.dto;

import java.math.BigDecimal;

public record CellDetailsDto(
        Integer cellId,
        Integer countrycode,
        String lastSeen,
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
