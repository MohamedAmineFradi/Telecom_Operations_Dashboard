package org.example.telecom_operations_dashboard.dto;

import java.math.BigDecimal;

public record CellTimeseriesPointDto(
        String datetime,
        BigDecimal sms,
        BigDecimal voice,
        BigDecimal data
) {}
