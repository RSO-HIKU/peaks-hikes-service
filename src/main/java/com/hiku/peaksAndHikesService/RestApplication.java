package com.hiku.peaksAndHikesService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application class to enable Jersey resource scanning.
 * Without this, Jersey doesn't know which resources to register.
 */
@ApplicationPath("/")
public class RestApplication extends Application {
    // No need to override anything - Jersey will auto-scan for @Path classes
} 
