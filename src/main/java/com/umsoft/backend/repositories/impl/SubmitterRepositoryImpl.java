package com.umsoft.backend.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.umsoft.backend.entities.Submitter;
import com.umsoft.backend.repositories.BaseRepository;
import com.umsoft.backend.repositories.SubmitterRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class SubmitterRepositoryImpl extends BaseRepository<Submitter, Long> implements SubmitterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public SubmitterRepositoryImpl() {
        super(Submitter.class);
    }

    @Override
    public List<Submitter> findAll() {
        return entityManager.createQuery("from Submitter s", Submitter.class)
                .getResultList();
    }

    @Override
    public Submitter findById(Long id) {
        return entityManager.find(Submitter.class, id);
    }

    @Override
    public Submitter create(Submitter submitter) {
        entityManager.persist(submitter);
        return submitter;
    }

    @Override
    public Submitter update(Submitter submitter) {
        return entityManager.merge(submitter);
    }

    @Override
    public void delete(Submitter submitter) {
        entityManager.remove(entityManager.contains(submitter) ? submitter : entityManager.merge(submitter));
    }

    @Override
    public Optional<Submitter> findByEmail(String email) {
        List<Submitter> result = entityManager.createQuery(
                        "from Submitter s where s.email = :email", Submitter.class)
                .setParameter("email", email)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = entityManager.createQuery(
                        "select count(s) from Submitter s where s.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }
}