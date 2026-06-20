package com.example.demo.dtos.reponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminProductResponse {
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
    private List<AdminProductVariantResponse> variants;
    private List<AdminProductImageResponse> images;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getConcentration() { return concentration; }
    public void setConcentration(String concentration) { this.concentration = concentration; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNormalizedKey() { return normalizedKey; }
    public void setNormalizedKey(String normalizedKey) { this.normalizedKey = normalizedKey; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<AdminProductVariantResponse> getVariants() { return variants; }
    public void setVariants(List<AdminProductVariantResponse> variants) { this.variants = variants; }
    public List<AdminProductImageResponse> getImages() { return images; }
    public void setImages(List<AdminProductImageResponse> images) { this.images = images; }
}
