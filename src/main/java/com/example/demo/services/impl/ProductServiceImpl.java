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

import javax.swing.text.html.Option;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final ProductFileReaderService productFileReaderService;

  @Override
  public Optional<ProductEntity> findProductById(Long id) {
    return productRepository.findProductById(id);
  }

  @Override
  public List<ProductEntity> getAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public Optional<ProductEntity> createProduct(ProductEntity product) {
    return Optional.of(productRepository.save(product));
  }

  @Override
  public Optional<ProductEntity> createProductWithImages(ProductImportFileRequest request) {
    List<ProductImageEntity> images = new ArrayList<>();

    if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
      for (int i = 0; i < request.getImageUrls().size(); i++) {
        ProductImageEntity image = ProductImageEntity.builder()
                .secureUrl(request.getImageUrls().get(i))
                .altText(request.getProductName())
                .isMain(i == 0)
                .sortOrder(i)
                .build();
        images.add(image);
      }
    }

    List<ProductVariantEntity> variants = new ArrayList<>();
    if (request.getVariants() != null && !request.getVariants().isEmpty()) {
      for (int i = 0; i < request.getVariants().size(); i++) {
        ProductVariantEntity variant = ProductVariantEntity.builder()
                .variantName(request.getVariants().get(i).getVariantName())
                .price(request.getVariants().get(i).getPrice())
                .sku(request.getVariants().get(i).getSku())
                .stockQuantity(request.getVariants().get(i).getStockQuantity())
                .volumeMl(request.getVariants().get(i).getVolumeMl())
                .build();
        variants.add(variant);
      }
    }

    ProductEntity product = ProductEntity.builder()
            .productName(request.getProductName())
            .brand(request.getBrand())
            .gender(request.getGender())
            .concentration(request.getConcentration())
            .releaseYear(request.getReleaseYear())
            .description(request.getDescription())
            .normalizedKey(request.getNormalizedKey())
            .price(request.getPrice())
            .images(images)
            .variants(variants)
            .build();
    return Optional.of(productRepository.save(product));
  }

  @Override
  public Optional<ProductEntity> updateProduct(Long id, ProductImportFileRequest request) {
    return productRepository.findById(id).map(product -> {
      product.setProductName(request.getProductName());
      product.setBrand(request.getBrand());
      product.setGender(request.getGender());
      product.setConcentration(request.getConcentration());
      product.setReleaseYear(request.getReleaseYear());
      product.setDescription(request.getDescription());
      product.setNormalizedKey(request.getNormalizedKey());
      product.setPrice(request.getPrice());

      if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
        product.getImages().clear();
        for (int i = 0; i < request.getImageUrls().size(); i++) {
          ProductImageEntity image = ProductImageEntity.builder()
                  .secureUrl(request.getImageUrls().get(i))
                  .altText(request.getProductName())
                  .isMain(i == 0)
                  .sortOrder(i)
                  .build();
          product.getImages().add(image);
        }
      }

      return productRepository.save(product);
    });
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

    List<ProductEntity> productEntities = new ArrayList<>();

    for (ProductImportFileRequest productRequest : products) {
      try {
        List<ProductVariantEntity> variants = new ArrayList<>();

        if (productRequest.getVariants() != null) {
          for (ProductVariantRequest variantRequest : productRequest.getVariants()) {
            List<ProductImageEntity> images = new ArrayList<>();
            if (variantRequest.getImages() != null) {
              images = variantRequest.getImages().stream()
                      .map(imgReq -> ProductImageEntity.builder()
                              .secureUrl(imgReq.getSecureUrl())
                              .altText(imgReq.getAltText() != null ? imgReq.getAltText() : "")
                              .isMain(imgReq.getIsMain() != null ? imgReq.getIsMain() : false)
                              .sortOrder(imgReq.getSortOrder() != null ? imgReq.getSortOrder() : 0)
                              .build())
                      .toList();
            }

            ProductVariantEntity variant = ProductVariantEntity.builder()
                    .sku(variantRequest.getSku())
                    .volumeMl(variantRequest.getVolumeMl())
                    .variantName(variantRequest.getVariantName())
                    .price(variantRequest.getPrice())
                    .stockQuantity(variantRequest.getStockQuantity() != null ? variantRequest.getStockQuantity() : 0)
                    .isActive(variantRequest.getIsActive() != null ? variantRequest.getIsActive() : true)
                    .build();
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

  @Override
  public Optional<ProductEntity> findByNormalizedKey(String normalizedKey) {
    return productRepository.findByNormalizedKey(normalizedKey);
  }
}
