package com.umsoft.backend.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

public abstract class BaseRepository<Entity, T> {
    private final Class<Entity> entityClass;

    public BaseRepository(Class<Entity> entityClass) {
        this.entityClass = entityClass;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Entity findById(Class<Entity> entityClass, T id) {
        return entityManager.find(entityClass, id);
    }

    @Transactional
    public Entity create(Entity entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Transactional
    public Entity update(Entity entity) {
        return entityManager.merge(entity);
    }

    @Transactional
    public void delete(Entity entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
}