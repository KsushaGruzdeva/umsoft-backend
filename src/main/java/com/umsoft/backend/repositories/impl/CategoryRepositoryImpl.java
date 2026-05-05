package com.umsoft.backend.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.umsoft.backend.entities.Category;
import com.umsoft.backend.repositories.BaseRepository;
import com.umsoft.backend.repositories.CategoryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class CategoryRepositoryImpl extends BaseRepository<Category, Long> implements CategoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public CategoryRepositoryImpl() {
        super(Category.class);
    }

    @Override
    public List<Category> findAll() {
        return entityManager.createQuery("from Category c", Category.class)
                .getResultList();
    }

    @Override
    public Category findById(Long id) {
        return entityManager.find(Category.class, id);
    }

    @Override
    public Category create(Category category) {
        entityManager.persist(category);
        return category;
    }

    @Override
    public Category update(Category category) {
        return entityManager.merge(category);
    }

    @Override
    public void delete(Category category) {
        entityManager.remove(entityManager.contains(category) ? category : entityManager.merge(category));
    }

    @Override
    public Optional<Category> findByName(String name) {
        List<Category> result = entityManager.createQuery(
                        "from Category c where c.name = :name", Category.class)
                .setParameter("name", name)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Category> findByDepartmentId(Long departmentId) {
        return entityManager.createQuery(
                        "from Category c where c.department.id = :departmentId", Category.class)
                .setParameter("departmentId", departmentId)
                .getResultList();
    }
}