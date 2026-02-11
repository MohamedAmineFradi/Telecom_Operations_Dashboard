package org.example.telecom_operations_dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fact_traffic_milan")
@IdClass(TrafficRecordId.class)
@Getter
@Setter
public class TrafficRecord {

    @Id
    @Column(name = "datetime", nullable = false)
    private OffsetDateTime datetime;

    @Id
    @Column(name = "cell_id", nullable = false)
    private Integer cellId;

    @Id
    @Column(name = "countrycode", nullable = false)
    private Integer countrycode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", insertable = false, updatable = false)
    private GridCell gridCell;

    @Column(name = "smsin", nullable = false)
    private BigDecimal smsin;
    @Column(name = "smsout", nullable = false)
    private BigDecimal smsout;
    @Column(name = "callin", nullable = false)
    private BigDecimal callin;
    @Column(name = "callout", nullable = false)
    private BigDecimal callout;
    @Column(name = "internet", nullable = false)
    private BigDecimal internet;

    // getters/setters
}
