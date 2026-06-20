package com.example.demo.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.reponse.ProductSearchResponse;
import com.example.demo.entities.ProductEntity;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.ProductSearchService;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {
    private final ProductRepository productRepository;

    public ProductSearchServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSearchResponse> searchProducts(String keyword, String brand, String gender, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findAll()
                .stream()
                .filter(product -> matchesKeyword(product, keyword))
                .filter(product -> matchesIgnoreCase(product.getBrand(), brand))
                .filter(product -> matchesIgnoreCase(product.getGender(), gender))
                .filter(product -> matchesMinPrice(product, minPrice))
                .filter(product -> matchesMaxPrice(product, maxPrice))
                .map(ProductSearchResponse::fromEntity)
                .toList();
    }

    private boolean matchesKeyword(ProductEntity product, String keyword) {
        if (!hasText(keyword)) {
            return true;
        }

        String lowerKeyword = keyword.trim().toLowerCase();
        return contains(product.getProductName(), lowerKeyword)
                || contains(product.getBrand(), lowerKeyword)
                || contains(product.getGender(), lowerKeyword)
                || contains(product.getConcentration(), lowerKeyword)
                || contains(product.getDescription(), lowerKeyword)
                || contains(product.getNormalizedKey(), lowerKeyword);
    }

    private boolean matchesIgnoreCase(String actualValue, String expectedValue) {
        if (!hasText(expectedValue)) {
            return true;
        }
        if (!hasText(actualValue)) {
            return false;
        }
        return actualValue.trim().equalsIgnoreCase(expectedValue.trim());
    }

    private boolean matchesMinPrice(ProductEntity product, BigDecimal minPrice) {
        if (minPrice == null || product.getPrice() == null) {
            return true;
        }
        return product.getPrice().compareTo(minPrice) >= 0;
    }

    private boolean matchesMaxPrice(ProductEntity product, BigDecimal maxPrice) {
        if (maxPrice == null || product.getPrice() == null) {
            return true;
        }
        return product.getPrice().compareTo(maxPrice) <= 0;
    }

    private boolean contains(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
