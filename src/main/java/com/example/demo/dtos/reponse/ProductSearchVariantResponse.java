package com.example.demo.dtos.reponse;
import com.example.demo.entities.ProductVariantEntity;
import lombok.*;

import java.math.BigDecimal;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchVariantResponse {
    private Long id;
    private String sku;
    private Integer volumeMl;
    private String variantName;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
    public static ProductSearchVariantResponse fromEntity(ProductVariantEntity variant) {
        ProductSearchVariantResponse response = new ProductSearchVariantResponse();
        response.setId(variant.getId());
        response.setSku(variant.getSku());
        response.setVolumeMl(variant.getVolumeMl());
        response.setVariantName(variant.getVariantName());
        response.setPrice(variant.getPrice());
        response.setStockQuantity(variant.getStockQuantity());
        response.setIsActive(variant.getIsActive());
        return response;
    }
}
