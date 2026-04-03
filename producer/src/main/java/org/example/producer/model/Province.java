package org.example.producer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dim_provinces_it")
public class Province {

    @Id
    @Column(name = "provincia", length = 50)
    private String provincia;

    @Column(name = "geometry", columnDefinition = "GEOMETRY(MULTIPOLYGON, 32632)", nullable = false)
    private String geometry;

    @Column(name = "population")
    private Integer population = 0;

    // getters/setters
}