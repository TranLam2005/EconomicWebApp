package com.example.demo.services;
import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.entities.ProductEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ProductService {
  Optional<ProductEntity> findProductById(Long id);

  Optional<ProductEntity> createProduct(ProductEntity product);

  ProductImportFileResponse importProduct(MultipartFile file);
}
