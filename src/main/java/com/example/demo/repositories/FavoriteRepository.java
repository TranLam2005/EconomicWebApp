package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.FavoriteEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.UserEntity;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    List<FavoriteEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    Optional<FavoriteEntity> findByUserAndProduct(UserEntity user, ProductEntity product);

    boolean existsByUserAndProduct(UserEntity user, ProductEntity product);
}