package com.hiku.peaksAndHikesService.service;

import com.hiku.peaksAndHikesService.db.dao.PeakDao;
import com.hiku.peaksAndHikesService.db.models.Peak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * PeakService handles business logic for peak operations.
 * This service layer decouples business logic from HTTP handling,
 * making the code more testable and reusable.
 */
@ApplicationScoped
public class PeakService {

    @Inject
    PeakDao peakDao;

    /**
     * Retrieves peaks based on an optional search query.
     * If query is null or empty, returns all peaks.
     * Otherwise, searches peaks by name.
     *
     * @param query Optional search query for peak name
     * @return List of Peak entities matching the criteria
     */
    public List<Peak> getPeaks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return peakDao.findAllPeaks();
        } else {
            return peakDao.searchPeaksByName(query);
        }
    }

    /**
     * Retrieves a single peak by ID.
     *
     * @param peakId The ID of the peak to retrieve
     * @return Peak entity if found, null otherwise
     */
    public Peak getPeakById(Long peakId) {
        return peakDao.findPeakById(peakId);
    }
}
