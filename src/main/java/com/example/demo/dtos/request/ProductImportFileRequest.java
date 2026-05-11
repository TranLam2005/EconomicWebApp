package com.example.demo.dtos.request;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImportFileRequest {
    private String productName;
    private String brand;
    private String gender;
    private String concentration;
    private Integer releaseYear;
    private String description;
    private String normalizedKey;
    private String secureUrl;
    private Integer volume;
    private BigDecimal price;
    private Integer stockQuantity;
}
