package com.example.demo.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.reponse.ProductSearchResponse;
import com.example.demo.services.ProductSearchService;

@RestController
public class ProductSearchController {
    private final ProductSearchService productSearchService;

    public ProductSearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping({"/products/search", "/public/products/search", "/api/products/search"})
    public ResponseEntity<List<ProductSearchResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(productSearchService.searchProducts(keyword, brand, gender, minPrice, maxPrice));
    }
}
