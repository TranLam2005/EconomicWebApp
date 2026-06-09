package com.example.demo.controllers;

import com.example.demo.dtos.request.BlogRequest;
import com.example.demo.dtos.reponse.BlogResponse;
import com.example.demo.entities.BlogEntity;
import com.example.demo.entities.CategoryEntity;
import com.example.demo.services.BlogService;
import com.example.demo.services.CategoryService;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pub/api/v1/blogs")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllBlogs() {
        List<BlogEntity> blogs = blogService.findAll();
        List<BlogResponse> responses = blogs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/published")
    public ResponseEntity<List<BlogResponse>> getPublishedBlogs() {
        List<BlogEntity> blogs = blogService.findAllPublished();
        List<BlogResponse> responses = blogs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        return blogService.findById(id)
                .map(blog -> {
                    blogService.incrementViewCount(id);
                    return ResponseEntity.ok(convertToResponse(blog));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlogResponse> getBlogBySlug(@PathVariable String slug) {
        return blogService.findBySlug(slug)
                .map(blog -> {
                    blogService.incrementViewCount(blog.getId());
                    return ResponseEntity.ok(convertToResponse(blog));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<BlogResponse>> getBlogsByCategory(@PathVariable Long categoryId) {
        return categoryService.findById(categoryId)
                .map(category -> {
                    List<BlogEntity> blogs = blogService.findByCategory(category);
                    List<BlogResponse> responses = blogs.stream()
                            .map(this::convertToResponse)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(responses);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(@RequestBody BlogRequest request, Authentication authentication) {
        String email = authentication.getName();
        var user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var category = categoryService.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        BlogEntity blog = new BlogEntity();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setSlug(request.getSlug());
        blog.setFeaturedImage(request.getFeaturedImage());
        blog.setExcerpt(request.getExcerpt());
        blog.setCategory(category);
        blog.setAuthor(user);
        blog.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : true);

        BlogEntity savedBlog = blogService.create(blog);
        return ResponseEntity.status(201).body(convertToResponse(savedBlog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(@PathVariable Long id, @RequestBody BlogRequest request) {
        try {
            BlogEntity blogDetails = new BlogEntity();
            blogDetails.setTitle(request.getTitle());
            blogDetails.setContent(request.getContent());
            blogDetails.setSlug(request.getSlug());
            blogDetails.setFeaturedImage(request.getFeaturedImage());
            blogDetails.setExcerpt(request.getExcerpt());
            blogDetails.setIsPublished(request.getIsPublished());

            if (request.getCategoryId() != null) {
                var category = categoryService.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                blogDetails.setCategory(category);
            }

            BlogEntity updatedBlog = blogService.update(id, blogDetails);
            return ResponseEntity.ok(convertToResponse(updatedBlog));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        try {
            blogService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private BlogResponse convertToResponse(BlogEntity blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setContent(blog.getContent());
        response.setSlug(blog.getSlug());
        response.setFeaturedImage(blog.getFeaturedImage());
        response.setExcerpt(blog.getExcerpt());
        response.setViewCount(blog.getViewCount());
        response.setCategoryId(blog.getCategory().getId());
        response.setCategoryName(blog.getCategory().getName());
        response.setAuthorId(blog.getAuthor().getId());
        response.setAuthorName(blog.getAuthor().getFirstName() + " " + blog.getAuthor().getLastName());
        response.setIsPublished(blog.getIsPublished());
        response.setCreatedAt(blog.getCreatedAt());
        response.setUpdatedAt(blog.getUpdatedAt());
        return response;
    }
}
