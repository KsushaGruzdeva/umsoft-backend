package com.umsoft.backend.repositories;

import com.umsoft.backend.entities.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {
    List<Department> findAll();
    Department findById(Long id);
    Department create(Department department);
    Department update(Department department);
    void delete(Department department);
    Optional<Department> findByName(String name);
    Integer count();
}