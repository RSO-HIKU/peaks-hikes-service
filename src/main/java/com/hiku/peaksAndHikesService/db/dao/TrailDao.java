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
        return em.createQuery(
                "SELECT t FROM Trail t WHERE t.id IN (" +
                        "SELECT MIN(t2.id) FROM Trail t2 GROUP BY t2.name, t2.lengthKm" +
                        ") ORDER BY t.name",
                Trail.class)
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

    public List<Trail> searchTrailsByName(String searchText) {
        return em.createQuery(
                "SELECT t FROM Trail t WHERE LOWER(t.name) LIKE LOWER(:searchText) " +
                "AND t.id IN (" +
                "SELECT MIN(t2.id) FROM Trail t2 " +
                "WHERE LOWER(t2.name) LIKE LOWER(:searchText) " +
                "GROUP BY t2.name, t2.lengthKm" +
                ") ORDER BY t.name",
                Trail.class)
                .setParameter("searchText", "%" + searchText + "%")
                .getResultList();
    }
}
