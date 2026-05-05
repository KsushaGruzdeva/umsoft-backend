package com.umsoft.backend.repositories;

import com.umsoft.backend.entities.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findAll();
    Category findById(Long id);
    Category create(Category category);
    Category update(Category category);
    void delete(Category category);
    Optional<Category> findByName(String name);
    List<Category> findByDepartmentId(Long departmentId);
}