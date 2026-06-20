package com.example.demo.dtos.request;

import java.math.BigDecimal;

public class AdminProductVariantRequest {
    private String sku;
    private Integer volumeMl;
    private String variantName;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getVolumeMl() { return volumeMl; }
    public void setVolumeMl(Integer volumeMl) { this.volumeMl = volumeMl; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
}
