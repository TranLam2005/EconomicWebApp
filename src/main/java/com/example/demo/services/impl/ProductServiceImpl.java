package com.example.demo.services.impl;

import com.example.demo.entities.ProductEntity;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.ProductService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Optional<ProductEntity> findProductById(Long id) {
    return productRepository.findById(id);
  }
}
