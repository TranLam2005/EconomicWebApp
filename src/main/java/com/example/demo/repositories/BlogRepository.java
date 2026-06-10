package com.example.demo.repositories;

import com.example.demo.entities.BlogEntity;
import com.example.demo.entities.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<BlogEntity, Long> {
    Optional<BlogEntity> findBySlug(String slug);
    List<BlogEntity> findByCategoryOrderByCreatedAtDesc(CategoryEntity category);
    List<BlogEntity> findByIsPublishedOrderByCreatedAtDesc(Boolean isPublished);
    @Query("SELECT b FROM BlogEntity b WHERE b.isPublished = true ORDER BY b.createdAt DESC")
    List<BlogEntity> findAllPublished();
}
