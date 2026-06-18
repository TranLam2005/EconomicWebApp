package com.example.demo.controllers;
import com.example.demo.dtos.request.ProductImportFileRequest;
import com.example.demo.entities.ProductEntity;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/public/product")
@Controller
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @PostMapping("/create")
  @ResponseBody
  public ResponseEntity<ProductEntity> create(@RequestBody ProductEntity product) {
    ProductEntity productSaved = productService.createProduct(product)
            .orElseThrow(() -> new RuntimeException("Product not saved"));
    return ResponseEntity.ok(productSaved);
  }

  @PostMapping("/create-with-images")
  @ResponseBody
  public ResponseEntity<ProductEntity> createWithImages(@RequestBody ProductImportFileRequest request) {
    ProductEntity productSaved = productService.createProductWithImages(request)
            .orElseThrow(() -> new RuntimeException("Product not saved"));
    return ResponseEntity.ok(productSaved);
  }

  @GetMapping("/{id}")
  @ResponseBody
  public ResponseEntity<ProductEntity> getProduct(@PathVariable Long id) {
    ProductEntity product = productService.findProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    return ResponseEntity.ok(product);
  }

  @PutMapping("/update/{id}")
  @ResponseBody
  public ResponseEntity<ProductEntity> updateProduct(
          @PathVariable Long id,
          @RequestBody ProductImportFileRequest request) {
    ProductEntity updated = productService.updateProduct(id, request)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    return ResponseEntity.ok(updated);
  }

  @GetMapping("/upload")
  public String upload(Model model) {
    return "pages/CreateListProduct";
  }

  @GetMapping("/listProduct")
  public ResponseEntity<List<ProductEntity>> getAllProducts() {
    List<ProductEntity> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }
}


