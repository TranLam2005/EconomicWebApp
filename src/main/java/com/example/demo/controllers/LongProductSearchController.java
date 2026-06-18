package com.example.demo.controllers;

import com.example.demo.dtos.reponse.LongProductCardResponse;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImageEntity;
import com.example.demo.entities.ProductVariantEntity;
import com.example.demo.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/long/products")
public class LongProductSearchController {
    private static final int DEFAULT_LIMIT = 60;

    private final ProductRepository productRepository;

    public LongProductSearchController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<LongProductCardResponse> searchProducts(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "concentration", required = false) String concentration,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "limit", defaultValue = "60") Integer limit
    ) {
        String keyword = clean(q);
        String brandValue = clean(brand);
        String genderValue = clean(gender);
        String concentrationValue = clean(concentration);

        Comparator<LongProductCardResponse> comparator = getComparator(sort);
        int safeLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 200);

        return productRepository.findAll().stream()
                .filter(product -> productId == null || Objects.equals(product.getId(), productId))
                .filter(product -> matchesKeyword(product, keyword))
                .filter(product -> contains(product.getBrand(), brandValue))
                .filter(product -> contains(product.getGender(), genderValue))
                .filter(product -> contains(product.getConcentration(), concentrationValue))
                .map(this::toCardResponse)
                .filter(card -> matchesPrice(card.getPrice(), minPrice, maxPrice))
                .sorted(comparator)
                .limit(safeLimit)
                .toList();
    }

    private LongProductCardResponse toCardResponse(ProductEntity product) {
        ProductVariantEntity variant = pickVariant(product);
        BigDecimal price = variant != null && variant.getPrice() != null ? variant.getPrice() : product.getPrice();

        return new LongProductCardResponse(
                product.getId(),
                product.getProductName(),
                product.getBrand(),
                product.getGender(),
                product.getConcentration(),
                price,
                pickImage(product, variant),
                variant != null ? variant.getId() : null,
                variant != null ? variant.getVariantName() : null,
                variant != null ? variant.getVolumeMl() : null,
                variant != null ? variant.getStockQuantity() : null
        );
    }

    private ProductVariantEntity pickVariant(ProductEntity product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return null;
        }

        return product.getVariants().stream()
                .filter(variant -> variant.getIsActive() == null || Boolean.TRUE.equals(variant.getIsActive()))
                .min(Comparator.comparing(ProductVariantEntity::getPrice, Comparator.nullsLast(BigDecimal::compareTo)))
                .orElse(product.getVariants().get(0));
    }

    private String pickImage(ProductEntity product, ProductVariantEntity variant) {
        String imageFromVariant = pickMainImage(variant != null ? variant.getImages() : null);
        if (imageFromVariant != null) {
            return imageFromVariant;
        }

        return pickMainImage(product.getImages());
    }

    private String pickMainImage(List<ProductImageEntity> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsMain()))
                .map(ProductImageEntity::getSecureUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElseGet(() -> images.stream()
                        .map(ProductImageEntity::getSecureUrl)
                        .filter(url -> url != null && !url.isBlank())
                        .findFirst()
                        .orElse(null));
    }

    private boolean matchesKeyword(ProductEntity product, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        return contains(product.getProductName(), keyword)
                || contains(product.getBrand(), keyword)
                || contains(product.getGender(), keyword)
                || contains(product.getConcentration(), keyword)
                || contains(product.getDescription(), keyword);
    }

    private boolean matchesPrice(BigDecimal price, BigDecimal minPrice, BigDecimal maxPrice) {
        if (price == null) {
            return minPrice == null && maxPrice == null;
        }

        boolean greaterThanMin = minPrice == null || price.compareTo(minPrice) >= 0;
        boolean lessThanMax = maxPrice == null || price.compareTo(maxPrice) <= 0;
        return greaterThanMin && lessThanMax;
    }

    private boolean contains(String source, String expected) {
        if (expected.isBlank()) {
            return true;
        }

        return source != null && clean(source).contains(expected);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private Comparator<LongProductCardResponse> getComparator(String sort) {
        return switch (sort == null ? "latest" : sort) {
            case "priceAsc" -> Comparator.comparing(LongProductCardResponse::getPrice, Comparator.nullsLast(BigDecimal::compareTo));
            case "priceDesc" -> Comparator.comparing(LongProductCardResponse::getPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed();
            case "nameAsc" -> Comparator.comparing(card -> clean(card.getProductName()));
            default -> Comparator.comparing(LongProductCardResponse::getProductId, Comparator.nullsLast(Long::compareTo)).reversed();
        };
    }
}
