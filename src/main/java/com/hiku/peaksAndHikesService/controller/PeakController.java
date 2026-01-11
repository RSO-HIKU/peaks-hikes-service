package com.hiku.peaksAndHikesService.controller;

import com.hiku.peaksAndHikesService.db.models.Peak;
import com.hiku.peaksAndHikesService.service.PeakService;

import org.locationtech.jts.geom.Point;

import javax.annotation.security.RolesAllowed;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PeakController handles HTTP requests for retrieving peak data.
 * 
 * <p><b>Purpose:</b>
 * Provides a REST endpoint that fetches all peaks from the database and returns them
 * in a lightweight JSON format optimized for Mapbox visualization.
 * 
 * <p><b>Nested Classes:</b>
 * 
 * <p><b>PeakDto:</b>
 * A Data Transfer Object that represents a peak in a simplified format suitable for
 * client consumption. It includes essential peak information (id, name, territory,
 * latitude, longitude, elevation) along with geographic data.
 * 
 * <p><b>GeometryDto:</b>
 * Converts JTS Point geometries into GeoJSON-compliant JSON structure.
 * This class ensures that peak locations are presented in the standard GeoJSON format
 * expected by mapping libraries like Mapbox.
 */
@Path("/getpeaks")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
public class PeakController {
    
    @Inject
    private PeakService peakService;
        
    @GET
    public Response getPeaks(@QueryParam("query") String query) {
        try {
            List<Peak> peaks = peakService.getPeaks(query);
            
            List<PeakDto> dto = peaks.stream()
                    .map(PeakDto::fromEntity)
                    .collect(Collectors.toList());
            
            return Response.ok(dto).build();
        } catch (Exception e) {
            System.err.println("Failed to fetch peaks: " + e.getMessage());
            return Response.serverError()
                    .entity("{\"error\":\"Unable to fetch peaks\"}" + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getPeakById(@PathParam("id") long id) {
        try {
            Peak peak = peakService.getPeakById(id);
            if (peak == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Peak not found\"}")
                        .build();
            }
            PeakDto dto = PeakDto.fromEntity(peak);
            return Response.ok(dto).build();
        } catch (Exception e) {
            System.err.println("Failed to fetch peak " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\":\"Unable to fetch peak: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /** Lightweight DTO with GeoJSON geometry for Mapbox. */
    public static class PeakDto {
        public Long id;
        public String name;
        public String territory;
        public Double latitude;
        public Double longitude;
        public Double elevationM;
        public GeometryDto geometry;

        static PeakDto fromEntity(Peak p) {
            PeakDto dto = new PeakDto();
            dto.id = p.getId();
            dto.name = p.getName();
            dto.territory = p.getTerritory();
            dto.latitude = p.getLatitude();
            dto.longitude = p.getLongitude();
            dto.elevationM = p.getElevationM();
            dto.geometry = GeometryDto.fromPoint(p.getGeom());
            return dto;
        }
    }

    /** Minimal GeoJSON geometry payload for Point. */
    public static class GeometryDto {
        public String type;
        public double[] coordinates;

        static GeometryDto fromPoint(Point point) {
            if (point == null) {
                return null;
            }
            GeometryDto g = new GeometryDto();
            g.type = "Point";
            g.coordinates = new double[2];
            g.coordinates[0] = point.getX(); // lon
            g.coordinates[1] = point.getY(); // lat
            return g;
        }
    }
}
