package org.example.producer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dim_grid_milan")
public class GridCell {

    @Id
    @Column(name = "cell_id")
    private Integer cellId;

    // geometry stored as String (WKT) or use JTS Geometry if PostGIS lib available
    @Column(name = "geometry", columnDefinition = "GEOMETRY(POLYGON, 32632)", nullable = false)
    private String geometry;

    @Column(name = "bounds")
    private String bounds;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // getters/setters
}