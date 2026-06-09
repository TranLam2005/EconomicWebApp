package com.example.demo.repositories;

import com.example.demo.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySlug(String slug);
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
}
