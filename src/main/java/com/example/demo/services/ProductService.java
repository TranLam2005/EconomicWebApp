package com.example.demo.services;
import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.dtos.request.ProductImportFileRequest;
import com.example.demo.entities.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface ProductService {
  Optional<ProductEntity> findProductById(Long id);

  List<ProductEntity> getAllProducts();

  Optional<ProductEntity> createProduct(ProductEntity product);

  Optional<ProductEntity> createProductWithImages(ProductImportFileRequest request);

  Optional<ProductEntity> updateProduct(Long id, ProductImportFileRequest request);

  ProductImportFileResponse importProduct(MultipartFile file);

  Optional<ProductEntity> findByNormalizedKey(String normalizedKey);

  Page<ProductEntity> findByGenderIgnoreCase(String gender, Pageable pageable);
}
