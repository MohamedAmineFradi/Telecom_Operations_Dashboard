package org.example.telecom_operations_dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class TrafficEvent {
    private OffsetDateTime datetime;
    private Integer cellId;
    private BigDecimal totalActivity;
    private BigDecimal totalSmsin;
    private BigDecimal totalSmsout;
    private BigDecimal totalCallin;
    private BigDecimal totalCallout;
    private BigDecimal totalInternet;
}
