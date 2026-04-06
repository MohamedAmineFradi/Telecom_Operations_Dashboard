package org.telecom_operations_dashboard.common.dto.internet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyInternetDto {
    private Integer cellId;
    private OffsetDateTime hour;
    private BigDecimal totalInternet;
}
