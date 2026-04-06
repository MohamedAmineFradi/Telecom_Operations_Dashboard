package org.example.producer.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "v_hourly_traffic")
@IdClass(HourlyTrafficRecordId.class)
@Getter
public class HourlyTrafficRecord {

    @Id
    @Column(name = "hour", nullable = false)
    private OffsetDateTime hour;

    @Id
    @Column(name = "cell_id", nullable = false)
    private Integer cellId;

    @Column(name = "total_smsin", nullable = false)
    private BigDecimal totalSmsin = BigDecimal.ZERO;

    @Column(name = "total_smsout", nullable = false)
    private BigDecimal totalSmsout = BigDecimal.ZERO;

    @Column(name = "total_callin", nullable = false)
    private BigDecimal totalCallin = BigDecimal.ZERO;

    @Column(name = "total_callout", nullable = false)
    private BigDecimal totalCallout = BigDecimal.ZERO;

    @Column(name = "total_internet", nullable = false)
    private BigDecimal totalInternet = BigDecimal.ZERO;

    @Column(name = "total_activity", nullable = false)
    private BigDecimal totalActivity = BigDecimal.ZERO;
}
