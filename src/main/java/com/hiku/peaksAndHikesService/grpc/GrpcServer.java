package com.hiku.peaksAndHikesService.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.io.IOException;
import java.util.logging.Logger;

@ApplicationScoped
public class GrpcServer {

    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());
    private static final int GRPC_PORT = 9090;
    
    private Server server;

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        try {
            start();
            logger.info("gRPC server started on port " + GRPC_PORT);
        } catch (Exception e) {
            logger.severe("Failed to start gRPC server: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void start() throws IOException {
        try {
            logger.info("Initializing gRPC server");
            
            // Create PeakServiceImpl - it will call internal REST endpoint
            PeakServiceImpl peakService = new PeakServiceImpl();
            logger.info("Created PeakServiceImpl");
            
            server = ServerBuilder.forPort(GRPC_PORT)
                    .addService((io.grpc.BindableService) peakService)
                    .build()
                    .start();
                    
            logger.info("gRPC server bound and started on port " + GRPC_PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down gRPC server");
                GrpcServer.this.stop();
            }));
        } catch (Exception e) {
            logger.severe("Error in start(): " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
