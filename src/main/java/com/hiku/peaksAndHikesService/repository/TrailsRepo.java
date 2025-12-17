package com.hiku.peaksAndHikesService.repository;

import com.hiku.shared.geoDataModels.Peak;
import com.hiku.shared.geoDataModels.Trail;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
public class TrailsRepo {

    private final EntityManagerFactory emf;

    public TrailsRepo() {
        // Create EntityManagerFactory for this service's persistence unit
        this.emf = Persistence.createEntityManagerFactory("peaksHikesPU");
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // ===== Trail Methods =====

    public List<Trail> findAllTrails() {
        EntityManager em = getEntityManager();
        try {
            // Explicitly query the schema-qualified table to avoid default schema issues
            return em.createNativeQuery("SELECT * FROM peaks_hikes_service.trails", Trail.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // public Trail findTrail(Long id) {
    //     EntityManager em = getEntityManager();
    //     Trail trail = em.find(Trail.class, id);
    //     em.close();
    //     return trail;
    // }

    // public Trail createTrail(Trail trail) {
    //     EntityManager em = getEntityManager();
    //     em.getTransaction().begin();
    //     em.persist(trail);
    //     em.getTransaction().commit();
    //     em.close();
    //     return trail;
    // }

    // public Trail updateTrail(Trail trail) {
    //     EntityManager em = getEntityManager();
    //     em.getTransaction().begin();
    //     Trail existing = em.find(Trail.class, trail.getId());
    //     if (existing == null) {
    //         em.getTransaction().rollback();
    //         em.close();
    //         return null;
    //     }
    //     Trail merged = em.merge(trail);
    //     em.getTransaction().commit();
    //     em.close();
    //     return merged;
    // }

    // public boolean deleteTrail(Long id) {
    //     EntityManager em = getEntityManager();
    //     em.getTransaction().begin();
    //     Trail t = em.find(Trail.class, id);
    //     if (t == null) {
    //         em.getTransaction().rollback();
    //         em.close();
    //         return false;
    //     }
    //     em.remove(t);
    //     em.getTransaction().commit();
    //     em.close();
    //     return true;
    // }

    // ===== Peak Methods =====

    // public List<Peak> findAllPeaks() {
    //     EntityManager em = getEntityManager();
    //     List<Peak> result = em.createQuery("SELECT p FROM Peak p", Peak.class).getResultList();
    //     em.close();
    //     return result;
    // }

    // public Peak findPeak(Long id) {
    //     EntityManager em = getEntityManager();
    //     Peak peak = em.find(Peak.class, id);
    //     em.close();
    //     return peak;
    // }

    // public Peak createPeak(Peak peak) {
    //     EntityManager em = getEntityManager();
    //     em.getTransaction().begin();
    //     em.persist(peak);
    //     em.getTransaction().commit();
    //     em.close();
    //     return peak;
    // }

    // public Peak updatePeak(Peak peak) {
    //     EntityManager em = getEntityManager();
    //     em.getTransaction().begin();
    //     Peak existing = em.find(Peak.class, peak.getId());
    //     if (existing == null) {
    //         em.getTransaction().rollback();
    //         em.close();
    //         return null;
    //     }
    //     Peak merged = em.merge(peak);
    //     em.getTransaction().commit();
    //     em.close();
    //     return merged;
    // }

    // public boolean deletePeak(Long id) {
    //     EntityManager em = getEntityManager();
    //     em.getTransaction().begin();
    //     Peak p = em.find(Peak.class, id);
    //     if (p == null) {
    //         em.getTransaction().rollback();
    //         em.close();
    //         return false;
    //     }
    //     em.remove(p);
    //     em.getTransaction().commit();
    //     em.close();
    //     return true;
    // }
}