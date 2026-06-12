package com.example.demo.dtos.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {
    private String sku;
    private Integer volumeMl;
    private String variantName;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
    private List<ProductImageRequest> images;
}
