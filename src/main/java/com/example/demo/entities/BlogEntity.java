package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String title;

    @Column(columnDefinition = "longtext")
    private String content;

    @Column(columnDefinition = "varchar(255)")
    private String slug;

    @Column(columnDefinition = "varchar(500)")
    private String featuredImage;

    @Column(columnDefinition = "longtext")
    private String excerpt;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isPublished = true;

    @Column(columnDefinition = "datetime", updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "datetime", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
