package com.example.demo.services;

import com.example.demo.entities.BrandEntity;
import java.util.List;
import java.util.Optional;

public interface BrandService {
    BrandEntity create(BrandEntity brand);
    List<BrandEntity> findAll();
    Optional<BrandEntity> findById(Long id);
    BrandEntity save(BrandEntity brand);
    BrandEntity update(Long id, BrandEntity brandDetails);
    void delete(Long id);
}
