package com.example.demo.entities;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_variant")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SVA-10
    @Column(nullable = false, unique = true, columnDefinition = "varchar(100)")
    private String sku;

    // 10
    @Column(name = "volume_ml", nullable = false)
    private Integer volumeMl;

    // 10ml
    @Column(name = "variant_name", nullable = false)
    private String variantName;

    // 3200000
    @Column(nullable = false, columnDefinition = "decimal(12,2)")
    private BigDecimal price;

    // 10
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }
}
