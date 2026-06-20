package com.example.demo.dtos.reponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImageEntity;
import com.example.demo.entities.ProductVariantEntity;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchResponse {
    private Long id;
    private String productName;
    private BigDecimal price;
    private String brand;
    private String gender;
    private String concentration;
    private Integer releaseYear;
    private String description;
    private String normalizedKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductSearchVariantResponse> variants = new ArrayList<>();
    private List<ProductSearchImageResponse> images = new ArrayList<>();

    public static ProductSearchResponse fromEntity(ProductEntity product) {
        ProductSearchResponse response = new ProductSearchResponse();
        response.setId(product.getId());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice());
        response.setBrand(product.getBrand());
        response.setGender(product.getGender());
        response.setConcentration(product.getConcentration());
        response.setReleaseYear(product.getReleaseYear());
        response.setDescription(product.getDescription());
        response.setNormalizedKey(product.getNormalizedKey());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getVariants() != null) {
            for (ProductVariantEntity variant : product.getVariants()) {
                response.getVariants().add(ProductSearchVariantResponse.fromEntity(variant));
            }
        }

        if (product.getImages() != null) {
            for (ProductImageEntity image : product.getImages()) {
                response.getImages().add(ProductSearchImageResponse.fromEntity(image));
            }
        }

        return response;
    }
}
