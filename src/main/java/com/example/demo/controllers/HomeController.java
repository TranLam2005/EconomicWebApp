package com.example.demo.controllers;

import com.example.demo.entities.BlogEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.repositories.BlogRepository;
import com.example.demo.repositories.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
public class HomeController {
    private final ProductRepository productRepository;
    private final BlogRepository blogRepository;

    public HomeController(ProductRepository productRepository, BlogRepository blogRepository) {
        this.productRepository = productRepository;
        this.blogRepository = blogRepository;
    }

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String home(Model model) {
        List<ProductEntity> allProducts = productRepository.findAll().stream()
                .sorted(Comparator.comparing(ProductEntity::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .toList();

        model.addAttribute("allProducts", allProducts.stream().limit(10).toList());
        model.addAttribute("menProducts", filterByGender(allProducts, "nam").stream().limit(5).toList());
        model.addAttribute("womenProducts", filterByGender(allProducts, "nữ", "nu").stream().limit(5).toList());
        model.addAttribute("unisexProducts", filterByGender(allProducts, "unisex").stream().limit(5).toList());
        model.addAttribute("articles", buildArticles());
        return "pages/home";
    }

    private List<ProductEntity> filterByGender(List<ProductEntity> products, String... keywords) {
        return products.stream()
                .filter(product -> {
                    String gender = normalize(product.getGender());
                    for (String keyword : keywords) {
                        if (gender.contains(normalize(keyword))) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private List<ArticleView> buildArticles() {
        List<BlogEntity> blogs = blogRepository.findAllPublished().stream()
                .limit(4)
                .toList();

        return blogs.stream()
                .map(blog -> new ArticleView(
                        blog.getTitle(),
                        blog.getSlug() != null && !blog.getSlug().isBlank() ? blog.getSlug() : String.valueOf(blog.getId()),
                        blog.getFeaturedImage() != null && !blog.getFeaturedImage().isBlank()
                                ? blog.getFeaturedImage()
                                : "https://placehold.co/400x300/084c3c/ffffff?text=PARFUMERIE+BLOG",
                        blog.getAuthor() != null ? blog.getAuthor().getEmail() : "Nhân viên Parfumerie",
                        blog.getCreatedAt() != null ? blog.getCreatedAt() : LocalDateTime.now(),
                        blog.getExcerpt() != null && !blog.getExcerpt().isBlank() ? blog.getExcerpt() : "Xem thêm bài viết tại Parfumerie."
                ))
                .toList();
    }

    public record ArticleView(
            String title,
            String slug,
            String thumbnailUrl,
            String author,
            LocalDateTime publishedAt,
            String excerpt
    ) {}
}
