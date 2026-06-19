package com.example.demo.dtos.request;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImportFileRequest {

    // Thông tin bảng product
    private String productName;

    private String brand;

    private String gender;

    private String concentration;

    private Integer releaseYear;

    private String description;

    private String normalizedKey;

    private BigDecimal price;

    // Thông tin bảng product_variant
    private String sku;

    private Integer volumeMl;

    private String variantName;

    private BigDecimal variantPrice;

    private Integer stockQuantity;

    private Boolean isActive;

    // Thông tin bảng product_image
    private String secureUrl;

    private String altText;

    private Boolean isMain;

    private Integer sortOrder;

    // Nested structures
    private List<ProductVariantRequest> variants;
    private List<ProductImageRequest> images;

    // Image URLs từ Cloudinary
    private List<String> imageUrls;

    private String mainImageUrl;
}