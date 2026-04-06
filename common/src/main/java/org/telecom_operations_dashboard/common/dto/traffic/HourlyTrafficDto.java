package org.telecom_operations_dashboard.common.dto.traffic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyTrafficDto {
    private OffsetDateTime hour;
    private Integer cellId;
    private BigDecimal totalSmsin;
    private BigDecimal totalSmsout;
    private BigDecimal totalCallin;
    private BigDecimal totalCallout;
    private BigDecimal totalInternet;
    private BigDecimal totalActivity;
}
