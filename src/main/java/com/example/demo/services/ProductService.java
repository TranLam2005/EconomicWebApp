package com.example.demo.services;
import com.example.demo.entities.ProductEntity;
import java.util.Optional;

public interface ProductService {
  Optional<ProductEntity> findProductById(Long id);
}
