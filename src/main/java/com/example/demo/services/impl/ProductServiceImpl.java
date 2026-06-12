package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.dtos.request.ProductImportFileRequest;
import com.example.demo.dtos.request.ProductVariantRequest;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImageEntity;
import com.example.demo.entities.ProductVariantEntity;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.ProductFileReaderService;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final ProductFileReaderService productFileReaderService;

  @Override
  public Optional<ProductEntity> findProductById(Long id) {
    return productRepository.findById(id);
  }

  @Override
  public Optional<ProductEntity> createProduct(ProductEntity product) {
    return Optional.of(productRepository.save(product));
  }

  @Override
  public ProductImportFileResponse importProduct(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new RuntimeException("File is empty");
    }

    String fileName = file.getOriginalFilename();

    List<ProductImportFileRequest> products;

    if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
      products = productFileReaderService.readFileCSV(file);
    } else if (fileName != null &&
            (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
      products = productFileReaderService.readFileExcel(file);
    } else {
      throw new RuntimeException("Only CSV or Excel files are allowed");
    }

    if (products.isEmpty()) {
      return ProductImportFileResponse.builder()
              .totalRows(0)
              .successRows(0)
              .failedRows(0)
              .build();
    }

    int totalProducts = products.size();
    int failedCount = 0;

    // Convert to ProductEntity
    List<ProductEntity> productEntities = new ArrayList<>();

    for (ProductImportFileRequest productRequest : products) {
      try {
        // Create variants from nested structure
        List<ProductVariantEntity> variants = new ArrayList<>();

        if (productRequest.getVariants() != null) {
          for (ProductVariantRequest variantRequest : productRequest.getVariants()) {
            // Create images
            List<ProductImageEntity> images = new ArrayList<>();
            if (variantRequest.getImages() != null) {
              images = variantRequest.getImages().stream()
                      .map(imgReq -> ProductImageEntity.builder()
                              .secureUrl(imgReq.getSecureUrl())
                              .altText(imgReq.getAltText() != null ? imgReq.getAltText() : "")
                              .isMain(imgReq.getIsMain() != null ? imgReq.getIsMain() : false)
                              .sortOrder(imgReq.getSortOrder() != null ? imgReq.getSortOrder() : 0)
                              .build())
                      .collect(Collectors.toList());
            }

            ProductVariantEntity variant = ProductVariantEntity.builder()
                    .sku(variantRequest.getSku())
                    .volumeMl(variantRequest.getVolumeMl())
                    .variantName(variantRequest.getVariantName())
                    .price(variantRequest.getPrice())
                    .stockQuantity(variantRequest.getStockQuantity() != null ? variantRequest.getStockQuantity() : 0)
                    .isActive(variantRequest.getIsActive() != null ? variantRequest.getIsActive() : true)
                    .images(images)
                    .build();

            images.forEach(img -> img.setVariant(variant));
            variants.add(variant);
          }
        }

        ProductEntity productEntity = ProductEntity.builder()
                .productName(productRequest.getProductName())
                .brand(productRequest.getBrand())
                .concentration(productRequest.getConcentration())
                .description(productRequest.getDescription() != null ? productRequest.getDescription() : "")
                .gender(productRequest.getGender())
                .normalizedKey(productRequest.getNormalizedKey())
                .price(productRequest.getPrice())
                .releaseYear(productRequest.getReleaseYear())
                .variants(variants)
                .build();

        variants.forEach(variant -> variant.setProduct(productEntity));
        productEntities.add(productEntity);
      } catch (Exception e) {
        failedCount++;
      }
    }

    productRepository.saveAll(productEntities);

    return ProductImportFileResponse.builder()
            .totalRows(totalProducts)
            .successRows(productEntities.size())
            .failedRows(failedCount)
            .build();
  }
}
