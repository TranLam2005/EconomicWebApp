package com.example.demo.dtos.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {
    private String secureUrl;
    private String altText;
    private Boolean isMain;
    private Integer sortOrder;
}
