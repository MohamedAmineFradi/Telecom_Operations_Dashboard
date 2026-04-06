package org.telecom_operations_dashboard.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongestionEvent {
    private Integer cellId;
    private OffsetDateTime hour;
    private BigDecimal totalActivity;
    private double score;
    private String severity;
}
