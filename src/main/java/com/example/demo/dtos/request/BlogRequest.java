package com.example.demo.dtos.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequest {
    private String title;
    private String content;
    private String slug;
    private String featuredImage;
    private String excerpt;
    private Long categoryId;
    private Boolean isPublished;
}
