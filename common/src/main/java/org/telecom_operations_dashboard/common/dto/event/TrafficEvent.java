package org.telecom_operations_dashboard.common.dto.event;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrafficEvent {
    private OffsetDateTime hour;
    private Integer        cellId;
    private BigDecimal     totalActivity;
}
