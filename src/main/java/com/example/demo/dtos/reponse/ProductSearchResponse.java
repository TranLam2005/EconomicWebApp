package com.example.demo.dtos.reponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImage;
import com.example.demo.entities.ProductVariant;

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
            for (ProductVariant variant : product.getVariants()) {
                response.getVariants().add(ProductSearchVariantResponse.fromEntity(variant));
            }
        }

        if (product.getImages() != null) {
            for (ProductImage image : product.getImages()) {
                response.getImages().add(ProductSearchImageResponse.fromEntity(image));
            }
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNormalizedKey() {
        return normalizedKey;
    }

    public void setNormalizedKey(String normalizedKey) {
        this.normalizedKey = normalizedKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ProductSearchVariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductSearchVariantResponse> variants) {
        this.variants = variants;
    }

    public List<ProductSearchImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ProductSearchImageResponse> images) {
        this.images = images;
    }
}
