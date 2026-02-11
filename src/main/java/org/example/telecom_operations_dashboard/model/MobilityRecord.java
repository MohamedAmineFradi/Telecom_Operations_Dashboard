package org.example.telecom_operations_dashboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fact_mobility_provinces")
@IdClass(MobilityRecordId.class)
@Getter
@Setter
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", insertable = false, updatable = false)
    private GridCell gridCell;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia", insertable = false, updatable = false)
    private Province province;

    @Column(name = "cell2province", nullable = false)
    private BigDecimal cell2province;

    @Column(name = "province2cell", nullable = false)
    private BigDecimal province2cell;
}
