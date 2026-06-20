package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "product")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name",  nullable = false, columnDefinition = "varchar(255)")
    private String productName;

    @Column()
    private BigDecimal price;

    @Column(name = "brand", nullable = false, columnDefinition = "varchar(100)")
    private String brand;

    @Column(name = "gender", nullable = false, columnDefinition = "varchar(20)")
    private String gender;

    @Column(name = "concentration", columnDefinition = "varchar(50)")
    private String concentration;

    @Column(name = "release_year", columnDefinition = "int")
    private Integer releaseYear;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false, unique = true)
    private String normalizedKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "product")
    private List<ProductVariantEntity> variants;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private List<ProductImageEntity> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderProductEntities;

    public void addVariant(ProductVariantEntity variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariantEntity variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    @Transient
    public String getPrimaryImageUrl() {
        if (images == null || images.isEmpty()) {
            return "";
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
                        .orElse(""));
    }

    @Transient
    public ProductVariantEntity getDisplayVariant() {
        if (variants == null || variants.isEmpty()) {
            return null;
        }

        return variants.stream()
                .filter(variant -> variant.getPrice() != null)
                .filter(variant -> variant.getIsActive() == null || Boolean.TRUE.equals(variant.getIsActive()))
                .min(Comparator.comparing(ProductVariantEntity::getPrice, Comparator.nullsLast(BigDecimal::compareTo)))
                .orElse(variants.get(0));
    }

    @Transient
    public Long getDisplayVariantId() {
        ProductVariantEntity variant = getDisplayVariant();
        return variant != null ? variant.getId() : null;
    }

    @Transient
    public BigDecimal getDisplayPrice() {
        ProductVariantEntity variant = getDisplayVariant();
        if (variant != null && variant.getPrice() != null) {
            return variant.getPrice();
        }
        return price;
    }

    @Transient
    public String getPriceRangeText() {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));

        if (variants != null && !variants.isEmpty()) {
            List<BigDecimal> prices = variants.stream()
                    .filter(variant -> variant.getIsActive() == null || Boolean.TRUE.equals(variant.getIsActive()))
                    .map(ProductVariantEntity::getPrice)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            if (!prices.isEmpty()) {
                BigDecimal min = prices.get(0);
                BigDecimal max = prices.get(prices.size() - 1);
                if (min.compareTo(max) == 0) {
                    return format.format(min) + "đ";
                }
                return format.format(min) + "đ - " + format.format(max) + "đ";
            }
        }

        if (price != null) {
            return format.format(price) + "đ";
        }

        return "Liên hệ";
    }
}
