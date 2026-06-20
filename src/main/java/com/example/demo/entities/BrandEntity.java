package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String slug;

    /**
     * Database hiện tại có cột is_active NOT NULL.
     * Nếu không set giá trị này khi insert, MySQL sẽ báo lỗi:
     * Field 'is_active' doesn't have a default value.
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = toSlug(this.name);
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = toSlug(this.name);
        }
    }

    private String toSlug(String value) {
        if (value == null) return null;
        return value.trim().toLowerCase()
                .replace("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
