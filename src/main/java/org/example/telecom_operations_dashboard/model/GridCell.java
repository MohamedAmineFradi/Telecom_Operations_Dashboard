package org.example.telecom_operations_dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Polygon;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dim_grid_milan")
@Getter
@Setter
@NoArgsConstructor
public class GridCell {

    @Id
    @Column(name = "cell_id")
    private Integer cellId;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "geometry", columnDefinition = "geometry(POLYGON,32632)", nullable = false)
    private Polygon geometry;

    @Column(name = "bounds")
    private String bounds;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "gridCell")
    private Set<TrafficRecord> trafficRecords = new HashSet<>();

    @OneToMany(mappedBy = "gridCell")
    private Set<MobilityRecord> mobilityRecords = new HashSet<>();
}
