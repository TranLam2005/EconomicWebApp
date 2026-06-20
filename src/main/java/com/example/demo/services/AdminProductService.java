package com.example.demo.services;

import com.example.demo.dtos.reponse.AdminProductResponse;
import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.dtos.request.AdminProductCreateRequest;
import com.example.demo.dtos.request.AdminProductUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminProductService {
    List<AdminProductResponse> getAllProducts();
    AdminProductResponse getProductById(Long id);
    AdminProductResponse createProduct(AdminProductCreateRequest request);
    AdminProductResponse updateProduct(Long id, AdminProductUpdateRequest request);
    void deleteProduct(Long id);
    void deleteProducts(List<Long> ids);
    ProductImportFileResponse importProducts(MultipartFile file);
}
