package com.example.demo.services;

import com.example.demo.entities.BlogEntity;
import com.example.demo.entities.CategoryEntity;
import java.util.List;
import java.util.Optional;

public interface BlogService {
    BlogEntity create(BlogEntity blog);
    BlogEntity update(Long id, BlogEntity blog);
    void delete(Long id);
    Optional<BlogEntity> findById(Long id);
    List<BlogEntity> findAll();
    Optional<BlogEntity> findBySlug(String slug);
    List<BlogEntity> findByCategory(CategoryEntity category);
    List<BlogEntity> findAllPublished();
    BlogEntity incrementViewCount(Long id);
}
