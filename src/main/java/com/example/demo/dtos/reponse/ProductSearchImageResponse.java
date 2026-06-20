package com.example.demo.dtos.reponse;
import com.example.demo.entities.ProductImageEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchImageResponse {
    private Long id;
    private String secureUrl;
    private String altText;
    private Boolean isMain;
    private Integer sortOrder;

    public static ProductSearchImageResponse fromEntity(ProductImageEntity image) {
        ProductSearchImageResponse response = new ProductSearchImageResponse();
        response.setId(image.getId());
        response.setSecureUrl(image.getSecureUrl());
        response.setAltText(image.getAltText());
        response.setIsMain(image.getIsMain());
        response.setSortOrder(image.getSortOrder());
        return response;
    }
}
