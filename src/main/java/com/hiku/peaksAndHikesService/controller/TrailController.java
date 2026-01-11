package com.hiku.peaksAndHikesService.controller;

import com.hiku.peaksAndHikesService.db.models.Trail;
import com.hiku.peaksAndHikesService.service.TrailService;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TrailController handles HTTP requests for retrieving trail data.
 * 
 * <p><b>Purpose:</b>
 * Provides a REST endpoint that fetches all trails from the database and returns them
 * in a lightweight JSON format optimized for Mapbox visualization.
 * 
 * <p><b>Nested Classes:</b>
 * 
 * <p><b>TrailDto:</b>
 * A Data Transfer Object that represents a trail in a simplified format suitable for
 * client consumption. It excludes unnecessary entity details and focuses on essential
 * trail information (id, name, lengthKm, sourceFile) along with geographic data.
 * This DTO is a bridge between the database entity and the REST response, enabling:
 * - Decoupling of the internal entity structure from the API contract
 * - Selective exposure of fields to clients
 * - Conversion of JPA entities to JSON-serializable objects
 * 
 * <p><b>GeometryDto:</b>
 * Converts JTS LineString geometries into GeoJSON-compliant JSON structure.
 * This class is necessary because:
 * - GeoJSON requires coordinates as [longitude, latitude] arrays in a specific format
 * - JTS LineString stores coordinates differently than GeoJSON expects
 * - Mapbox (the frontend mapping library) requires standard GeoJSON geometry objects
 * - Provides a clean separation between geospatial domain objects and presentation format
 * 
 */

@Path("/gettrails")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
public class TrailController {
    
    @Inject
    private TrailService trailService;
        
    @GET
    public Response getTrails(@QueryParam("query") String query) {
        try {
            List<Trail> trails = trailService.getTrails(query);
            
            List<TrailDto> dto = trails.stream()
                    .map(TrailDto::fromEntity)
                    .collect(Collectors.toList());
            
            return Response.ok(dto).build();
        } catch (Exception e) {
            System.err.println("Failed to fetch trails: " + e.getMessage());
            return Response.serverError()
                    .entity("{\"error\":\"Unable to fetch trails\"}"+ e.getMessage())
                    .build();
        }
    }

    /** Lightweight DTO with GeoJSON geometry for Mapbox. */
    public static class TrailDto {
        public Long id; 
        public String name;
        public Double lengthKm;
        public String sourceFile;
        public GeometryDto geometry;

        static TrailDto fromEntity(Trail t) {
            TrailDto dto = new TrailDto();
            dto.id = t.getId();
            dto.name = t.getName();
            dto.lengthKm = t.getLengthKm();
            dto.sourceFile = t.getSourceFile();
            dto.geometry = GeometryDto.fromLineString(t.getGeometry());
            return dto;
        }
    }

    /** Minimal GeoJSON geometry payload. */
    public static class GeometryDto {
        public String type;
        public double[][] coordinates;

        static GeometryDto fromLineString(LineString line) {
            if (line == null) {
                return null;
            }
            GeometryDto g = new GeometryDto();
            g.type = "LineString";
            Coordinate[] coords = line.getCoordinates();
            g.coordinates = new double[coords.length][2];
            for (int i = 0; i < coords.length; i++) {
                g.coordinates[i][0] = coords[i].x; // lon
                g.coordinates[i][1] = coords[i].y; // lat
            }
            return g;
        }
    }
}
