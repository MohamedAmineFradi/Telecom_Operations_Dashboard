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
    private OffsetDateTime datetime;

    @Id
    @Column(name = "cell_id")
    private Integer cellId;

    @Id
    private Integer countrycode;

    private BigDecimal smsin;
    private BigDecimal smsout;
    private BigDecimal callin;
    private BigDecimal callout;
    private BigDecimal internet;

    // getters/setters
}
