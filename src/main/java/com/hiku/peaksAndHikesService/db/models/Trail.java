package com.hiku.peaksAndHikesService.db.models;

import javax.persistence.*;
import java.time.OffsetDateTime;
import org.locationtech.jts.geom.LineString;

@Entity
@Table(name = "trails", schema = "peaks_hikes_service")
public class Trail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "length_km")
    private Double lengthKm;

    @Column(nullable = false, columnDefinition = "geometry(LineString,4326)")
    private LineString geometry;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "source_file")
    private String sourceFile;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    // getters / setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLengthKm() {
        return lengthKm;
    }

    public void setLengthKm(Double lengthKm) {
        this.lengthKm = lengthKm;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
}
