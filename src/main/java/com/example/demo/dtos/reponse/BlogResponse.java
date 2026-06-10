package com.example.demo.dtos.reponse;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String slug;
    private String featuredImage;
    private String excerpt;
    private Integer viewCount;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
