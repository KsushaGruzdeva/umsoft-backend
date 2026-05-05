package com.umsoft.backend.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.umsoft.backend.entities.Department;
import com.umsoft.backend.repositories.BaseRepository;
import com.umsoft.backend.repositories.DepartmentRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DepartmentRepositoryImpl extends BaseRepository<Department, Long> implements DepartmentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public DepartmentRepositoryImpl() {
        super(Department.class);
    }

    @Override
    public List<Department> findAll() {
        return entityManager.createQuery("from Department d", Department.class)
                .getResultList();
    }

    @Override
    public Department findById(Long id) {
        return entityManager.find(Department.class, id);
    }

    @Override
    public Department create(Department department) {
        entityManager.persist(department);
        return department;
    }

    @Override
    public Department update(Department department) {
        return entityManager.merge(department);
    }

    @Override
    public void delete(Department department) {
        entityManager.remove(entityManager.contains(department) ? department : entityManager.merge(department));
    }

    @Override
    public Optional<Department> findByName(String name) {
        List<Department> result = entityManager.createQuery(
                        "from Department d where d.name = :name", Department.class)
                .setParameter("name", name)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Integer count() {
        return entityManager.createQuery("from Department d", Department.class)
                .getResultList().size();
    }
}