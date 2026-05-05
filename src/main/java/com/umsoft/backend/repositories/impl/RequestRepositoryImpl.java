package com.umsoft.backend.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.umsoft.backend.entities.Request;
import com.umsoft.backend.entities.Submitter;
import com.umsoft.backend.repositories.BaseRepository;
import com.umsoft.backend.repositories.RequestRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class RequestRepositoryImpl extends BaseRepository<Request, Long> implements RequestRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public RequestRepositoryImpl() {
        super(Request.class);
    }

    @Override
    public List<Request> findAll() {
        return entityManager.createQuery("from Request r order by r.createdAt desc", Request.class)
                .getResultList();
    }

    @Override
    public Request findById(Long id) {
        return entityManager.find(Request.class, id);
    }

    @Override
    public Request create(Request request) {
        entityManager.persist(request);
        return request;
    }

    @Override
    public Request update(Request request) {
        return entityManager.merge(request);
    }

    @Override
    public void delete(Request request) {
        entityManager.remove(entityManager.contains(request) ? request : entityManager.merge(request));
    }

    @Override
    public List<Request> findBySubmitter(Submitter submitter) {
        return entityManager.createQuery(
                        "from Request r where r.submitter = :submitter order by r.createdAt desc", Request.class)
                .setParameter("submitter", submitter)
                .getResultList();
    }

    @Override
    public List<Request> findByStatus(String status) {
        return entityManager.createQuery(
                        "from Request r where r.status = :status order by r.createdAt desc", Request.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public Optional<Request> findByIdWithDetails(Long id) {
        List<Request> result = entityManager.createQuery(
                        "select distinct r from Request r " +
                                "left join fetch r.submitter " +
                                "left join fetch r.category " +
                                "left join fetch r.assignedDepartment " +
                                "where r.id = :id", Request.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findFirst();
    }
}