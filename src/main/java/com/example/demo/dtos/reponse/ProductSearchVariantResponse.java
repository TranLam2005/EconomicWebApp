package com.example.demo.dtos.reponse;

import java.math.BigDecimal;

import com.example.demo.entities.ProductVariant;

public class ProductSearchVariantResponse {
    private Long id;
    private String sku;
    private Integer volumeMl;
    private String variantName;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;

    public static ProductSearchVariantResponse fromEntity(ProductVariant variant) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(Integer volumeMl) {
        this.volumeMl = volumeMl;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
