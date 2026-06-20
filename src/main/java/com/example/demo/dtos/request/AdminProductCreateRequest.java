package com.example.demo.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public class AdminProductCreateRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá sản phẩm không được âm")
    private BigDecimal price;

    @NotBlank(message = "Thương hiệu không được để trống")
    private String brand;

    @NotBlank(message = "Giới tính không được để trống")
    private String gender;

    private String concentration;
    private Integer releaseYear;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    private String normalizedKey;

    @Valid
    private List<AdminProductVariantRequest> variants;

    @Valid
    private List<AdminProductImageRequest> images;

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
    public List<AdminProductVariantRequest> getVariants() { return variants; }
    public void setVariants(List<AdminProductVariantRequest> variants) { this.variants = variants; }
    public List<AdminProductImageRequest> getImages() { return images; }
    public void setImages(List<AdminProductImageRequest> images) { this.images = images; }
}
