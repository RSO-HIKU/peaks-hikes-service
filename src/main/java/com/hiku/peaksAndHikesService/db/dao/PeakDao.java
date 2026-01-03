package com.hiku.peaksAndHikesService.db.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.hiku.peaksAndHikesService.db.models.Peak;

import java.util.List;

@ApplicationScoped
public class PeakDao {

    @Inject
    private EntityManager em;

    public List<Peak> findAllPeaks() {
        return em.createQuery("SELECT p FROM Peak p ORDER BY p.name", Peak.class)
                .getResultList();
    }

    public Peak findPeakById(Long id) {
        try {
            return em.createQuery(
                    "SELECT p FROM Peak p WHERE p.id = :id",
                    Peak.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    public List<Peak> searchPeaksByName(String searchText) {
        return em.createQuery(
                "SELECT p FROM Peak p WHERE LOWER(p.name) LIKE LOWER(:searchText) ORDER BY p.name",
                Peak.class)
                .setParameter("searchText", "%" + searchText + "%")
                .getResultList();
    }
}
