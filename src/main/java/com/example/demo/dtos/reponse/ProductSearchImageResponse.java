package com.example.demo.dtos.reponse;

import com.example.demo.entities.ProductImage;

public class ProductSearchImageResponse {
    private Long id;
    private String secureUrl;
    private String altText;
    private Boolean isMain;
    private Integer sortOrder;

    public static ProductSearchImageResponse fromEntity(ProductImage image) {
        ProductSearchImageResponse response = new ProductSearchImageResponse();
        response.setId(image.getId());
        response.setSecureUrl(image.getSecureUrl());
        response.setAltText(image.getAltText());
        response.setIsMain(image.getIsMain());
        response.setSortOrder(image.getSortOrder());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean main) {
        isMain = main;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
