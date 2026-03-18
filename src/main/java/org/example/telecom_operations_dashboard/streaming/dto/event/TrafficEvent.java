package org.example.telecom_operations_dashboard.streaming.dto.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class TrafficEvent {
    private OffsetDateTime datetime;
    private Integer cellId;
    private Integer countrycode;
    private BigDecimal totalActivity;
    private BigDecimal totalSmsin;
    private BigDecimal totalSmsout;
    private BigDecimal totalCallin;
    private BigDecimal totalCallout;
    private BigDecimal totalInternet;
}
