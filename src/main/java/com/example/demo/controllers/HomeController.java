package com.example.demo.controllers;

import com.example.demo.entities.ProductEntity;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
  private final ProductService productService;

  @GetMapping("/")
  public String home() {
    return "pages/home";
  }

  @GetMapping("/dashboard")
  public String dashboard() {
    return "pages/dashboard";
  }

  @GetMapping("/importProduct")
  public String importProduct() {
    return "pages/importProduct";
  }

  @GetMapping("/createProduct")
  public String createProduct() {
    return "pages/createProduct";
  }

  @GetMapping("/listProduct")
  public String listProduct(Model model) {
    List<ProductEntity> products = productService.getAllProducts();
    model.addAttribute("products", products);
    return "pages/listProduct";
  }

  @GetMapping("/editProduct/{id}")
  public String editProduct(@PathVariable Long id, Model model) {
    ProductEntity product = productService.findProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product cannot find"));
    model.addAttribute("product", product);
    model.addAttribute("productId", id);
    return "pages/editProduct";
  }
}
