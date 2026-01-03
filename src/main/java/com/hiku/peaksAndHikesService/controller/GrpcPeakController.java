package com.hiku.peaksAndHikesService.controller;

import com.hiku.peaksAndHikesService.db.models.Peak;
import com.hiku.peaksAndHikesService.service.PeakService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Internal REST endpoint for gRPC service to fetch peak data.
 * This is called internally by the gRPC service to avoid Weld proxy issues.
 */
@ApplicationScoped
@Path("/internal/peaks")
@Produces(MediaType.APPLICATION_JSON)
public class GrpcPeakController {
    
    private static final Logger logger = Logger.getLogger(GrpcPeakController.class.getName());
    
    @Inject
    private PeakService peakService;
    
    @GET
    @Path("/{peakId}")
    public Response getPeakForGrpc(@PathParam("peakId") Long peakId) {
        try {
            logger.info("gRPC internal call: fetching peak " + peakId);
            
            Peak peak = peakService.getPeakById(peakId);
            
            if (peak == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Peak not found\"}")
                        .build();
            }
            
            // Return as JSON
            PeakDto dto = new PeakDto(
                    peak.getId(),
                    peak.getName(),
                    peak.getTerritory(),
                    peak.getElevationM()
            );
            
            return Response.ok(dto).build();
        } catch (Exception e) {
            logger.severe("Error fetching peak: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    /**
     * DTO for gRPC peak response
     */
    public static class PeakDto {
        public Long id;
        public String name;
        public String territory;
        public Double elevationM;
        
        public PeakDto() {}
        
        public PeakDto(Long id, String name, String territory, Double elevationM) {
            this.id = id;
            this.name = name;
            this.territory = territory;
            this.elevationM = elevationM;
        }
    }
}
