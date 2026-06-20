package com.example.demo.controllers;

import com.example.demo.dtos.reponse.AdminProductResponse;
import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.dtos.request.AdminProductCreateRequest;
import com.example.demo.dtos.request.AdminProductUpdateRequest;
import com.example.demo.services.AdminProductService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {
    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public ResponseEntity<List<AdminProductResponse>> getAllProducts() {
        return ResponseEntity.ok(adminProductService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<AdminProductResponse> createProduct(@RequestBody @Valid AdminProductCreateRequest request) {
        return ResponseEntity.ok(adminProductService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminProductResponse> updateProduct(@PathVariable Long id,
                                                              @RequestBody @Valid AdminProductUpdateRequest request) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.ok("Đã xóa sản phẩm id = " + id);
    }

    @DeleteMapping("/bulk-delete")
    public ResponseEntity<String> deleteProducts(@RequestBody List<Long> ids) {
        adminProductService.deleteProducts(ids);
        return ResponseEntity.ok("Đã xóa " + ids.size() + " sản phẩm");
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImportFileResponse> importProducts(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminProductService.importProducts(file));
    }
}
