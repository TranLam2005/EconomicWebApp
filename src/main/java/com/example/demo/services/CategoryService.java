package com.example.demo.services;

import com.example.demo.entities.CategoryEntity;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryEntity create(CategoryEntity category);
    CategoryEntity update(Long id, CategoryEntity category);
    void delete(Long id);
    Optional<CategoryEntity> findById(Long id);
    List<CategoryEntity> findAll();
    Optional<CategoryEntity> findBySlug(String slug);
}
