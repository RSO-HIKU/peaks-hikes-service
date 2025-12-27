package com.hiku.peaksAndHikesService.db.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.hiku.peaksAndHikesService.db.models.Trail;

import java.util.List;

@ApplicationScoped
public class TrailDao {

    @Inject
    private EntityManager em;

    public List<Trail> findAllTrails() {
        return em.createQuery("SELECT t FROM Trail t", Trail.class)
                .getResultList();
    }

    public Trail findTrailById(Long id) {
        return em.find(Trail.class, id);
    }

    public void deleteTrail(Long id) {
        Trail trail = findTrailById(id);
        if (trail != null) {
            em.getTransaction().begin();
            em.remove(trail);
            em.getTransaction().commit();
        }
    }
}
