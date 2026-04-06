package org.telecom_operations_dashboard.common.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlySmsDto {
    private Integer cellId;
    private OffsetDateTime hour;
    private BigDecimal totalSmsin;
    private BigDecimal totalSmsout;
    private BigDecimal totalActivity;
}
