package com.example.demo.dtos.reponse;

import java.math.BigInteger;

public class AdminProductImageResponse {
    private Long id;
    private String cloudinaryAssetId;
    private String publicId;
    private String versionNo;
    private String resourceType;
    private String format;
    private String secureUrl;
    private Integer width;
    private Integer height;
    private BigInteger bytesSize;
    private String altText;
    private Boolean isMain;
    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCloudinaryAssetId() { return cloudinaryAssetId; }
    public void setCloudinaryAssetId(String cloudinaryAssetId) { this.cloudinaryAssetId = cloudinaryAssetId; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getSecureUrl() { return secureUrl; }
    public void setSecureUrl(String secureUrl) { this.secureUrl = secureUrl; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public BigInteger getBytesSize() { return bytesSize; }
    public void setBytesSize(BigInteger bytesSize) { this.bytesSize = bytesSize; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    public Boolean getIsMain() { return isMain; }
    public void setIsMain(Boolean main) { isMain = main; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
