package com.hiku.peaksAndHikesService.db.models;

import javax.persistence.*;
import java.time.OffsetDateTime;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "peaks", schema = "peaks_hikes_service")
public class Peak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String territory;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "elevation_m")
    private Double elevationM;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    // Getters and setters

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

    public String getTerritory() {
        return territory;
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getElevationM() {
        return elevationM;
    }

    public void setElevationM(Double elevationM) {
        this.elevationM = elevationM;
    }

    public Point getGeom() {
        return geom;
    }

    public void setGeom(Point geom) {
        this.geom = geom;
    }
}
