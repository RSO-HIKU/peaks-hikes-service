package com.hiku.peaksAndHikesService.grpc;

import com.hiku.grpc.peak.PeakRequest;
import com.hiku.grpc.peak.PeakResponse;
import com.hiku.grpc.peak.PeakServiceGrpc;
import io.grpc.stub.StreamObserver;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class PeakServiceImpl extends PeakServiceGrpc.PeakServiceImplBase {

    private static final Logger logger = Logger.getLogger(PeakServiceImpl.class.getName());
    private static final String INTERNAL_ENDPOINT = System.getenv("GRPC_INTERNAL_ENDPOINT") != null ? System.getenv("GRPC_INTERNAL_ENDPOINT") : "http://localhost:8082/getpeaks/";

    @Override
    public void getPeakById(PeakRequest request, StreamObserver<PeakResponse> responseObserver) {
        try {
            int peakId = request.getPeakId();
            logger.info("gRPC: Fetching peak with ID: " + peakId);
            
            // Call internal REST endpoint
            URL url = new URL(INTERNAL_ENDPOINT + peakId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int statusCode = conn.getResponseCode();
            logger.info("gRPC: Internal endpoint responded with status: " + statusCode);
            
            PeakResponse.Builder responseBuilder = PeakResponse.newBuilder();
            
            if (statusCode == 200) {
                // Parse JSON response
                JsonReader jsonReader = Json.createReader(conn.getInputStream());
                JsonObject jsonObject = jsonReader.readObject();
                
                responseBuilder
                    .setId(peakId)
                    .setName(jsonObject.getString("name", ""))
                    .setTerritory(jsonObject.getString("territory", ""))
                    .setElevationM(jsonObject.getJsonNumber("elevationM") != null ? 
                        jsonObject.getJsonNumber("elevationM").doubleValue() : 0.0)
                    .setFound(true);
                
                logger.info("gRPC: Peak found - " + jsonObject.getString("name"));
            } else if (statusCode == 404) {
                responseBuilder
                    .setId(peakId)
                    .setFound(false);
                logger.info("gRPC: Peak not found");
            } else {
                throw new Exception("Internal endpoint returned status " + statusCode);
            }
            
            conn.disconnect();
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            logger.info("gRPC: Response sent successfully for peak " + peakId);
        } catch (Exception e) {
            logger.severe("gRPC error in getPeakById: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }
}
