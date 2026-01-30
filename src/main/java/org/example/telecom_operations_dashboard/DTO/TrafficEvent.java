package org.example.telecom_operations_dashboard.DTO;

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

    public void setDatetime(OffsetDateTime hour) {

    }
    // + sms, voice, data breakdown
}
