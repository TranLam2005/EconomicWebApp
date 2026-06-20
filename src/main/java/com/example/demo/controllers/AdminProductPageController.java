package com.example.demo.controllers;

import com.example.demo.entities.ProductEntity;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminProductPageController {

    private final ProductService productService;

    @GetMapping({"/listProduct", "/local-admin/products"})
    public String listProduct(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "pages/listProduct";
    }

    @GetMapping({"/createProduct", "/local-admin/products/create"})
    public String createProduct() {
        return "pages/createProduct";
    }

    @GetMapping({"/importProduct", "/local-admin/products/import"})
    public String importProduct() {
        return "pages/importProduct";
    }

    @GetMapping({"/editProduct/{id}", "/local-admin/products/{id}/edit"})
    public String editProduct(@PathVariable Long id, Model model) {
        ProductEntity product = productService.findProductById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id = " + id));
        model.addAttribute("product", product);
        return "pages/editProduct";
    }

    @GetMapping("/public/product/list")
    @ResponseBody
    public ResponseEntity<List<ProductEntity>> getPublicProductList() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
