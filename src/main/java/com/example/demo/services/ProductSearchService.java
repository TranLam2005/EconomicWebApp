package com.example.demo.services;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.dtos.reponse.ProductSearchResponse;

public interface ProductSearchService {
    List<ProductSearchResponse> searchProducts(String keyword, String brand, String gender, BigDecimal minPrice, BigDecimal maxPrice);
}
