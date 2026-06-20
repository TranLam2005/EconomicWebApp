package com.example.demo.controllers;

import com.example.demo.entities.BlogEntity;
import com.example.demo.entities.CategoryEntity;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.services.BlogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class FrontendBlogController {
    private static final DateTimeFormatter VI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int DEFAULT_PAGE_SIZE = 3;

    private final BlogService blogService;
    private final CategoryRepository categoryRepository;

    public FrontendBlogController(BlogService blogService, CategoryRepository categoryRepository) {
        this.blogService = blogService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping({"/blog", "/blogs"})
    public String blogList(@RequestParam(value = "categoryId", required = false) Long categoryId,
                           @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           Model model) {
        List<CategoryEntity> categories = categoryRepository.findAll().stream()
                .filter(category -> category.getName() != null && !category.getName().isBlank())
                .sorted(Comparator.comparing(category -> category.getName().toLowerCase(Locale.ROOT)))
                .toList();

        List<BlogEntity> sourceBlogs;
        String selectedCategoryName = null;

        if (categoryId != null) {
            Optional<CategoryEntity> selectedCategory = categoryRepository.findById(categoryId);
            if (selectedCategory.isPresent()) {
                selectedCategoryName = selectedCategory.get().getName();
                sourceBlogs = blogService.findByCategory(selectedCategory.get());
            } else {
                sourceBlogs = blogService.findAllPublished();
                categoryId = null;
            }
        } else {
            sourceBlogs = blogService.findAllPublished();
        }

        List<BlogCard> allBlogs = sourceBlogs.stream()
                .filter(blog -> Boolean.TRUE.equals(blog.getIsPublished()))
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(BlogCard::from)
                .toList();

        int totalItems = allBlogs.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) DEFAULT_PAGE_SIZE));
        int currentPage = page == null ? 1 : page;
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = Math.min((currentPage - 1) * DEFAULT_PAGE_SIZE, totalItems);
        int toIndex = Math.min(fromIndex + DEFAULT_PAGE_SIZE, totalItems);
        List<BlogCard> blogs = allBlogs.subList(fromIndex, toIndex);

        model.addAttribute("blogs", blogs);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCategoryName", selectedCategoryName);
        model.addAttribute("blogCount", totalItems);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", buildPageNumbers(currentPage, totalPages));
        model.addAttribute("hasPrevious", currentPage > 1);
        model.addAttribute("hasNext", currentPage < totalPages);
        model.addAttribute("previousPage", Math.max(1, currentPage - 1));
        model.addAttribute("nextPage", Math.min(totalPages, currentPage + 1));
        model.addAttribute("isAdmin", isCurrentUserAdmin());
        return "pages/blog-list";
    }

    @GetMapping({"/blog/{slugOrId}", "/blogs/{slugOrId}"})
    public String blogDetail(@PathVariable String slugOrId, Model model, RedirectAttributes redirectAttributes) {
        Optional<BlogEntity> foundBlog = blogService.findBySlug(slugOrId);

        if (foundBlog.isEmpty() && slugOrId.matches("\\d+")) {
            foundBlog = blogService.findById(Long.parseLong(slugOrId));
        }

        if (foundBlog.isEmpty() || !Boolean.TRUE.equals(foundBlog.get().getIsPublished())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bài viết blog hoặc bài viết chưa được hiển thị.");
            return "redirect:/blog";
        }

        BlogEntity blog = blogService.incrementViewCount(foundBlog.get().getId());
        BlogDetail detail = BlogDetail.from(blog);

        List<BlogCard> relatedBlogs = blogService.findAllPublished().stream()
                .filter(item -> !item.getId().equals(blog.getId()))
                .filter(item -> Boolean.TRUE.equals(item.getIsPublished()))
                .limit(3)
                .map(BlogCard::from)
                .toList();

        model.addAttribute("blog", detail);
        model.addAttribute("relatedBlogs", relatedBlogs);
        return "pages/blog-detail-public";
    }

    private static List<Integer> buildPageNumbers(int currentPage, int totalPages) {
        List<Integer> pages = new ArrayList<>();
        if (totalPages <= 7) {
            for (int i = 1; i <= totalPages; i++) pages.add(i);
            return pages;
        }

        int start = Math.max(1, currentPage - 2);
        int end = Math.min(totalPages, currentPage + 2);

        if (start > 1) pages.add(1);
        for (int i = start; i <= end; i++) {
            if (!pages.contains(i)) pages.add(i);
        }
        if (end < totalPages) pages.add(totalPages);
        return pages;
    }

    private static boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ADMIN".equals(role) || "ROLE_ADMIN".equals(role));
    }

    private static String blogUrl(BlogEntity blog) {
        String slug = blog.getSlug();
        if (slug == null || slug.isBlank()) {
            return "/blog/" + blog.getId();
        }
        return "/blog/" + URLEncoder.encode(slug.trim(), StandardCharsets.UTF_8);
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeExcerpt(BlogEntity blog) {
        String excerpt = safeText(blog.getExcerpt());
        if (!excerpt.isBlank()) {
            return excerpt;
        }

        String content = safeText(blog.getContent()).replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        if (content.length() <= 190) {
            return content;
        }
        return content.substring(0, 190) + "...";
    }

    private static String formatDate(LocalDateTime value) {
        return value == null ? "" : value.format(VI_DATE_FORMAT);
    }

    public static class BlogCard {
        private final Long id;
        private final String title;
        private final String excerpt;
        private final String featuredImage;
        private final String categoryName;
        private final String authorName;
        private final String createdAt;
        private final Integer viewCount;
        private final String url;

        private BlogCard(Long id, String title, String excerpt, String featuredImage,
                         String categoryName, String authorName, String createdAt,
                         Integer viewCount, String url) {
            this.id = id;
            this.title = title;
            this.excerpt = excerpt;
            this.featuredImage = featuredImage;
            this.categoryName = categoryName;
            this.authorName = authorName;
            this.createdAt = createdAt;
            this.viewCount = viewCount;
            this.url = url;
        }

        public static BlogCard from(BlogEntity blog) {
            String categoryName = blog.getCategory() == null ? "Blog" : safeText(blog.getCategory().getName());
            String authorName = blog.getAuthor() == null
                    ? "Parfumerie"
                    : (safeText(blog.getAuthor().getFirstName()) + " " + safeText(blog.getAuthor().getLastName())).trim();
            if (authorName.isBlank()) {
                authorName = "Parfumerie";
            }

            return new BlogCard(
                    blog.getId(),
                    safeText(blog.getTitle()),
                    safeExcerpt(blog),
                    safeText(blog.getFeaturedImage()),
                    categoryName,
                    authorName,
                    formatDate(blog.getCreatedAt()),
                    blog.getViewCount() == null ? 0 : blog.getViewCount(),
                    blogUrl(blog)
            );
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getExcerpt() { return excerpt; }
        public String getFeaturedImage() { return featuredImage; }
        public String getCategoryName() { return categoryName; }
        public String getAuthorName() { return authorName; }
        public String getCreatedAt() { return createdAt; }
        public Integer getViewCount() { return viewCount; }
        public String getUrl() { return url; }
    }

    public static class BlogDetail {
        private final Long id;
        private final String title;
        private final String content;
        private final String excerpt;
        private final String featuredImage;
        private final String categoryName;
        private final String authorName;
        private final String createdAt;
        private final Integer viewCount;

        private BlogDetail(Long id, String title, String content, String excerpt, String featuredImage,
                           String categoryName, String authorName, String createdAt, Integer viewCount) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.excerpt = excerpt;
            this.featuredImage = featuredImage;
            this.categoryName = categoryName;
            this.authorName = authorName;
            this.createdAt = createdAt;
            this.viewCount = viewCount;
        }

        public static BlogDetail from(BlogEntity blog) {
            BlogCard card = BlogCard.from(blog);
            return new BlogDetail(
                    blog.getId(),
                    card.getTitle(),
                    safeText(blog.getContent()),
                    card.getExcerpt(),
                    card.getFeaturedImage(),
                    card.getCategoryName(),
                    card.getAuthorName(),
                    card.getCreatedAt(),
                    card.getViewCount()
            );
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getExcerpt() { return excerpt; }
        public String getFeaturedImage() { return featuredImage; }
        public String getCategoryName() { return categoryName; }
        public String getAuthorName() { return authorName; }
        public String getCreatedAt() { return createdAt; }
        public Integer getViewCount() { return viewCount; }
    }
}
