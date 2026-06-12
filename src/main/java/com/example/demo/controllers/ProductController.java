package com.example.demo.controllers;
import com.example.demo.entities.ProductEntity;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/public/product")
@Controller
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @PostMapping("/create")
  public ResponseEntity<ProductEntity> create(@RequestBody ProductEntity product) {
    ProductEntity productSaved = productService.createProduct(product)
            .orElseThrow(() -> new RuntimeException("Product not saved"));
    return ResponseEntity.ok(productSaved);
  }

  @GetMapping("/upload")
  public String upload(Model model) {
    return "pages/CreateListProduct";
  }
}
