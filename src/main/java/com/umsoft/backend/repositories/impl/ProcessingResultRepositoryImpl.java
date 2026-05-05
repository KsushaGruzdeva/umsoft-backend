package com.umsoft.backend.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.umsoft.backend.entities.ProcessingResult;
import com.umsoft.backend.entities.Request;
import com.umsoft.backend.repositories.BaseRepository;
import com.umsoft.backend.repositories.ProcessingResultRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class ProcessingResultRepositoryImpl extends BaseRepository<ProcessingResult, Long> implements ProcessingResultRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ProcessingResultRepositoryImpl() {
        super(ProcessingResult.class);
    }

    @Override
    public List<ProcessingResult> findAll() {
        return entityManager.createQuery("from ProcessingResult pr", ProcessingResult.class)
                .getResultList();
    }

    @Override
    public ProcessingResult findById(Long id) {
        return entityManager.find(ProcessingResult.class, id);
    }

    @Override
    public ProcessingResult create(ProcessingResult processingResult) {
        entityManager.persist(processingResult);
        return processingResult;
    }

    @Override
    public ProcessingResult update(ProcessingResult processingResult) {
        return entityManager.merge(processingResult);
    }

    @Override
    public void delete(ProcessingResult processingResult) {
        entityManager.remove(entityManager.contains(processingResult) ? processingResult : entityManager.merge(processingResult));
    }

    @Override
    public Optional<ProcessingResult> findByRequest(Request request) {
        List<ProcessingResult> result = entityManager.createQuery(
                        "from ProcessingResult pr where pr.request = :request", ProcessingResult.class)
                .setParameter("request", request)
                .getResultList();
        return result.stream().findFirst();
    }
}