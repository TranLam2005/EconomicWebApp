package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // Tom Ford Ombre Leather
    @Column(name = "product_name",  nullable = false, columnDefinition = "varchar(255)")
    private String productName;

    @Column()
    private BigDecimal price;

    // Tom Ford
    @Column(name = "brand", nullable = false, columnDefinition = "varchar(100)")
    private String brand;

    // Unisex
    @Column(name = "gender", nullable = false, columnDefinition = "varchar(20)")
    private String gender;

    // EDP
    @Column(name = "concentration", columnDefinition = "varchar(50)")
    private String concentration;

    // 2018
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
        syncVariantParents();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        syncVariantParents();
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "product")
    @Builder.Default
    private List<ProductVariantEntity> variants = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    @Builder.Default
    private List<ProductImageEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderProductEntities;

    public void setVariants(List<ProductVariantEntity> variants) {
        this.variants = variants != null ? variants : new ArrayList<>();
        syncVariantParents();
    }

    public void addVariant(ProductVariantEntity variant) {
        if (variants == null) {
            variants = new ArrayList<>();
        }
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariantEntity variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    private void syncVariantParents() {
        if (variants == null) {
            return;
        }
        variants.forEach(variant -> variant.setProduct(this));
    }

    @Transient
    public BigDecimal getMinPrice() {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return variants.stream()
                .map(ProductVariantEntity::getPrice)
                .filter(java.util.Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    @Transient
    public BigDecimal getMaxPrice() {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return variants.stream()
                .map(ProductVariantEntity::getPrice)
                .filter(java.util.Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(null);
    }

    @Transient
    public int getTotalStock() {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        return variants.stream()
                .map(ProductVariantEntity::getStockQuantity)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
