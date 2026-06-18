package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_image")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // abc123xyz
  @Column(name = "cloudinary_asset_id", columnDefinition = "varchar(255)")
  private String cloudinaryAssetId;

  // perfume/products/tom-ford-ombre-leather/main
  @Column(name = "public_id", columnDefinition = "varchar(255)")
  private String publicId;

  // 1712345678
  @Column(name = "version_no", columnDefinition = "varchar(50)")
  private String versionNo;

  // image
  @Column(name = "resource_type", columnDefinition = "varchar(20)")
  private String resourceType;

  // jpg
  @Column(columnDefinition = "varchar(20)")
  private String format;

  // https://res.cloudinary.com/your-cloud/image/upload/v1712345678/perfume/products/tom-ford-ombre-leather/main.jpg
  @Column(columnDefinition = "varchar(500)", name = "secure_url")
  private String secureUrl;

  private Integer width;

  private Integer height;

  // Tom Ford Ombre Leather main image
  @Column(name = "alt_text", columnDefinition = "varchar(255)")
  private String altText;

  // true
  @Column(name = "is_main")
  private Boolean isMain;

  // 1
  @Column(name = "sort_order")
  private Integer sortOrder;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.resourceType = "image";
  }
}
