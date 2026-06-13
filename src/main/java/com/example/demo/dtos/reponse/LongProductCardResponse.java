package com.example.demo.dtos.reponse;

import java.math.BigDecimal;

public class LongProductCardResponse {
    private final Long productId;
    private final String productName;
    private final String brand;
    private final String gender;
    private final String concentration;
    private final BigDecimal price;
    private final String imageUrl;
    private final Long variantId;
    private final String variantName;
    private final Integer volumeMl;
    private final Integer stockQuantity;

    public LongProductCardResponse(
            Long productId,
            String productName,
            String brand,
            String gender,
            String concentration,
            BigDecimal price,
            String imageUrl,
            Long variantId,
            String variantName,
            Integer volumeMl,
            Integer stockQuantity
    ) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.gender = gender;
        this.concentration = concentration;
        this.price = price;
        this.imageUrl = imageUrl;
        this.variantId = variantId;
        this.variantName = variantName;
        this.volumeMl = volumeMl;
        this.stockQuantity = stockQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getBrand() {
        return brand;
    }

    public String getGender() {
        return gender;
    }

    public String getConcentration() {
        return concentration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getVariantId() {
        return variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public Integer getVolumeMl() {
        return volumeMl;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }
}
