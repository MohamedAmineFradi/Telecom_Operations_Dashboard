package org.example.producer.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "fact_mobility_provinces")
@IdClass(MobilityRecordId.class)
public class MobilityRecord {

    @Id
    @Column(name = "datetime", nullable = false)
    private OffsetDateTime datetime;

    @Id
    @Column(name = "cell_id", nullable = false)
    private Integer cellId;

    @Id
    @Column(name = "provincia", length = 50, nullable = false)
    private String provincia;

    @Column(name = "cell2province", nullable = false)
    private BigDecimal cell2province = BigDecimal.ZERO;

    @Column(name = "province2cell", nullable = false)
    private BigDecimal province2cell = BigDecimal.ZERO;

    // getters/setters
}