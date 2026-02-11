package org.example.telecom_operations_dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "dim_provinces_it")
@Getter
@Setter
@NoArgsConstructor
public class Province {

    @Id
    @Column(name = "provincia", length = 50)
    private String provincia;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "geometry", columnDefinition = "geometry(MULTIPOLYGON,32632)", nullable = false)
    private MultiPolygon geometry;

    @Column(name = "population")
    private Integer population;

    @OneToMany(mappedBy = "province")
    private Set<MobilityRecord> mobilityRecords = new HashSet<>();
}
