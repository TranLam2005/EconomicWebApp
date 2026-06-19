package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private List<ProductVariantEntity> variants;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private List<ProductImageEntity> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity>  orderProductEntities;
}
