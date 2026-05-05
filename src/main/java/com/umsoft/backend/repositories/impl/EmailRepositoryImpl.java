package com.umsoft.backend.repositories.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.umsoft.backend.entities.Email;
import com.umsoft.backend.entities.Request;
import com.umsoft.backend.repositories.BaseRepository;
import com.umsoft.backend.repositories.EmailRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class EmailRepositoryImpl extends BaseRepository<Email, Long> implements EmailRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public EmailRepositoryImpl() {
        super(Email.class);
    }

    @Override
    public List<Email> findAll() {
        return entityManager.createQuery("from Email e order by e.createdAt desc", Email.class)
                .getResultList();
    }

    @Override
    public Email findById(Long id) {
        return entityManager.find(Email.class, id);
    }

    @Override
    public Email create(Email email) {
        entityManager.persist(email);
        return email;
    }

    @Override
    public Email update(Email email) {
        return entityManager.merge(email);
    }

    @Override
    public void delete(Email email) {
        entityManager.remove(entityManager.contains(email) ? email : entityManager.merge(email));
    }

    @Override
    public List<Email> findByRequest(Request request) {
        return entityManager.createQuery(
                        "from Email e where e.request = :request order by e.createdAt desc", Email.class)
                .setParameter("request", request)
                .getResultList();
    }

    @Override
    public List<Email> findByStatus(String status) {
        return entityManager.createQuery(
                        "from Email e where e.status = :status order by e.createdAt desc", Email.class)
                .setParameter("status", status)
                .getResultList();
    }
}