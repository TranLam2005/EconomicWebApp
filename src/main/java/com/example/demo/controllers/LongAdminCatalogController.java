package com.example.demo.controllers;

import com.example.demo.entities.BrandEntity;
import com.example.demo.entities.CategoryEntity;
import com.example.demo.repositories.BrandRepository;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class LongAdminCatalogController {
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final BrandService brandService;
    private final ProductRepository productRepository;

    public LongAdminCatalogController(CategoryRepository categoryRepository,
                                      BrandRepository brandRepository,
                                      BrandService brandService,
                                      ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.brandService = brandService;
        this.productRepository = productRepository;
    }

    /**
     * Trang admin chính /local-admin/catalog đã được AdminBackendController xử lý.
     * Không map trùng đường dẫn đó ở đây để tránh lỗi Ambiguous handler methods.
     * Trang AJAX cũ được giữ lại ở đường dẫn phụ nếu cần dùng kiểm thử.
     */
    @GetMapping({"/local-admin/catalog-ajax", "/local-admin/categories-ajax", "/local-admin/brands-ajax"})
    public String catalogPage() {
        return "pages/admin/long-catalog-management";
    }

    @GetMapping("/api/long/catalog/menu")
    @ResponseBody
    public CatalogMenuResponse catalogMenu() {
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(category -> safeLower(category.getName())))
                .map(this::toCategoryResponse)
                .toList();

        List<BrandMenuResponse> brands = brandRepository.findAll().stream()
                .filter(brand -> brand.getActive() == null || Boolean.TRUE.equals(brand.getActive()))
                .sorted(Comparator.comparing(brand -> safeLower(brand.getName())))
                .map(brand -> new BrandMenuResponse(brand.getId(), brand.getName(), brand.getSlug(), brand.getActive()))
                .toList();

        return new CatalogMenuResponse(categories, brands);
    }

    @GetMapping("/api/long-admin/categories")
    @ResponseBody
    public List<CategoryResponse> categories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(category -> safeLower(category.getName())))
                .map(this::toCategoryResponse)
                .toList();
    }

    @PostMapping("/api/long-admin/categories")
    @ResponseBody
    public ResponseEntity<?> createCategory(@RequestBody CatalogRequest request) {
        try {
            String name = clean(request.name());
            if (name.isBlank()) {
                return badRequest("Tên danh mục không được để trống");
            }
            if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
                return badRequest("Danh mục đã tồn tại");
            }

            CategoryEntity category = new CategoryEntity();
            category.setName(name);
            category.setDescription(clean(request.description()));
            category.setSlug(uniqueCategorySlug(cleanOrGenerateSlug(request.slug(), name), null));
            CategoryEntity saved = categoryRepository.save(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(toCategoryResponse(saved));
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping("/api/long-admin/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CatalogRequest request) {
        try {
            CategoryEntity category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

            String name = clean(request.name());
            if (!name.isBlank() && !name.equalsIgnoreCase(category.getName())) {
                categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Danh mục đã tồn tại");
                    }
                });
                category.setName(name);
            }

            if (request.description() != null) {
                category.setDescription(clean(request.description()));
            }

            if (request.slug() != null || !name.isBlank()) {
                String baseName = !name.isBlank() ? name : category.getName();
                category.setSlug(uniqueCategorySlug(cleanOrGenerateSlug(request.slug(), baseName), id));
            }

            CategoryEntity saved = categoryRepository.save(category);
            return ResponseEntity.ok(toCategoryResponse(saved));
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/api/long-admin/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            if (!categoryRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Không tìm thấy danh mục"));
            }
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return badRequest("Không thể xóa danh mục: " + e.getMessage());
        }
    }

    @GetMapping("/api/long-admin/brands")
    @ResponseBody
    public List<BrandResponse> brands() {
        return brandRepository.findAll().stream()
                .sorted(Comparator.comparing(brand -> safeLower(brand.getName())))
                .map(this::toBrandResponse)
                .toList();
    }

    @PostMapping("/api/long-admin/brands")
    @ResponseBody
    public ResponseEntity<?> createBrand(@RequestBody CatalogRequest request) {
        try {
            BrandEntity brand = new BrandEntity();
            brand.setName(clean(request.name()));
            brand.setDescription(clean(request.description()));
            brand.setSlug(clean(request.slug()));
            brand.setActive(request.active() == null || request.active());
            BrandEntity saved = brandService.create(brand);
            return ResponseEntity.status(HttpStatus.CREATED).body(toBrandResponse(saved));
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping("/api/long-admin/brands/{id}")
    @ResponseBody
    public ResponseEntity<?> updateBrand(@PathVariable Long id, @RequestBody CatalogRequest request) {
        try {
            BrandEntity details = new BrandEntity();
            details.setName(clean(request.name()));
            details.setDescription(request.description());
            details.setSlug(request.slug());
            details.setActive(request.active());
            BrandEntity saved = brandService.update(id, details);
            return ResponseEntity.ok(toBrandResponse(saved));
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/api/long-admin/brands/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBrand(@PathVariable Long id) {
        try {
            if (!brandRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Không tìm thấy thương hiệu"));
            }
            brandService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return badRequest("Không thể xóa thương hiệu: " + e.getMessage());
        }
    }

    private CategoryResponse toCategoryResponse(CategoryEntity category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSlug(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private BrandResponse toBrandResponse(BrandEntity brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getSlug(),
                brand.getActive(),
                productRepository.countByBrandIgnoreCase(brand.getName()),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ErrorResponse(message == null || message.isBlank() ? "Dữ liệu không hợp lệ" : message));
    }

    private String uniqueCategorySlug(String baseSlug, Long currentId) {
        String safeBase = baseSlug == null || baseSlug.isBlank() ? "danh-muc" : baseSlug;
        String candidate = safeBase;
        int index = 2;
        while (true) {
            Optional<CategoryEntity> existing = categoryRepository.findBySlug(candidate);
            if (existing.isEmpty() || existing.get().getId().equals(currentId)) {
                return candidate;
            }
            candidate = safeBase + "-" + index++;
        }
    }

    private String cleanOrGenerateSlug(String slug, String name) {
        String source = clean(slug).isBlank() ? name : slug;
        return toSlug(source);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeLower(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input == null ? "" : input, Normalizer.Form.NFD);
        String withoutMarks = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return withoutMarks.toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    public record CatalogRequest(String name, String description, String slug, Boolean active) {}
    public record ErrorResponse(String message) {}
    public record CategoryResponse(Long id, String name, String description, String slug, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record BrandResponse(Long id, String name, String description, String slug, Boolean active, Long productCount, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record BrandMenuResponse(Long id, String name, String slug, Boolean active) {}
    public record CatalogMenuResponse(List<CategoryResponse> categories, List<BrandMenuResponse> brands) {}
}
