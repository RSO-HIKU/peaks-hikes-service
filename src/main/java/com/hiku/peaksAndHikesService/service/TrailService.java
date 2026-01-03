package com.hiku.peaksAndHikesService.service;

import com.hiku.peaksAndHikesService.db.dao.TrailDao;
import com.hiku.peaksAndHikesService.db.models.Trail;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * TrailService handles business logic for trail operations.
 * This service layer decouples business logic from HTTP handling,
 * making the code more testable and reusable.
 */
@ApplicationScoped
public class TrailService {

    @Inject
    TrailDao trailDao;

    /**
     * Retrieves trails based on an optional search query.
     * If query is null or empty, returns all trails.
     * Otherwise, searches trails by name.
     *
     * @param query Optional search query for trail name
     * @return List of Trail entities matching the criteria
     */
    public List<Trail> getTrails(String query) {
        if (query == null || query.trim().isEmpty()) {
            return trailDao.findAllTrails();
        } else {
            return trailDao.searchTrailsByName(query);
        }
    }
}
