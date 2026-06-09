package com.example.demo.services.impl;

import com.example.demo.entities.BlogEntity;
import com.example.demo.entities.CategoryEntity;
import com.example.demo.repositories.BlogRepository;
import com.example.demo.services.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BlogServiceImpl implements BlogService {
    @Autowired
    private BlogRepository blogRepository;

    @Override
    public BlogEntity create(BlogEntity blog) {
        return blogRepository.save(blog);
    }

    @Override
    public BlogEntity update(Long id, BlogEntity blogDetails) {
        BlogEntity blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (blogDetails.getTitle() != null) {
            blog.setTitle(blogDetails.getTitle());
        }
        if (blogDetails.getContent() != null) {
            blog.setContent(blogDetails.getContent());
        }
        if (blogDetails.getSlug() != null) {
            blog.setSlug(blogDetails.getSlug());
        }
        if (blogDetails.getFeaturedImage() != null) {
            blog.setFeaturedImage(blogDetails.getFeaturedImage());
        }
        if (blogDetails.getExcerpt() != null) {
            blog.setExcerpt(blogDetails.getExcerpt());
        }
        if (blogDetails.getCategory() != null) {
            blog.setCategory(blogDetails.getCategory());
        }
        if (blogDetails.getIsPublished() != null) {
            blog.setIsPublished(blogDetails.getIsPublished());
        }

        return blogRepository.save(blog);
    }

    @Override
    public void delete(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new RuntimeException("Blog not found");
        }
        blogRepository.deleteById(id);
    }

    @Override
    public Optional<BlogEntity> findById(Long id) {
        return blogRepository.findById(id);
    }

    @Override
    public List<BlogEntity> findAll() {
        return blogRepository.findAll();
    }

    @Override
    public Optional<BlogEntity> findBySlug(String slug) {
        return blogRepository.findBySlug(slug);
    }

    @Override
    public List<BlogEntity> findByCategory(CategoryEntity category) {
        return blogRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    @Override
    public List<BlogEntity> findAllPublished() {
        return blogRepository.findAllPublished();
    }

    @Override
    public BlogEntity incrementViewCount(Long id) {
        BlogEntity blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        blog.setViewCount(blog.getViewCount() + 1);
        return blogRepository.save(blog);
    }
}
