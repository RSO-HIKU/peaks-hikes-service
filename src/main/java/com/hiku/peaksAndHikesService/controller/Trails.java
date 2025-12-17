package com.hiku.peaksAndHikesService.controller;

import com.hiku.peaksAndHikesService.repository.TrailsRepo;
import com.hiku.shared.geoDataModels.Trail;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/trails")
@Produces(MediaType.APPLICATION_JSON)
public class Trails {

    private final TrailsRepo trailsRepo = new TrailsRepo();

    @GET
    public Response getAllTrails() {
        try {
            List<Trail> trails = trailsRepo.findAllTrails();
            List<TrailDto> dto = trails.stream()
                    .map(TrailDto::fromEntity)
                    .collect(Collectors.toList());
            return Response.ok(dto).build();
        } catch (Exception e) {
            System.err.println("Failed to fetch trails: " + e.getMessage());
            return Response.serverError()
                    .entity("{\"error\":\"Unable to fetch trails\"}")
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
