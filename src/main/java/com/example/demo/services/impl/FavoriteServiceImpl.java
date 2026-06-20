package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.FavoriteResponse;
import com.example.demo.entities.FavoriteEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImageEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.repositories.FavoriteRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.FavoriteService;
import com.example.demo.services.ShopUserResolverService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ShopUserResolverService shopUserResolverService;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public FavoriteServiceImpl(
            FavoriteRepository favoriteRepository,
            ShopUserResolverService shopUserResolverService,
            ProductRepository productRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.shopUserResolverService = shopUserResolverService;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(String email) {
        UserEntity user = getUserByEmail(email);

        return favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FavoriteResponse addFavorite(String email, Long productId) {
        UserEntity user = getUserByEmail(email);
        ProductEntity product = getProductById(productId);

        return favoriteRepository.findByUserAndProduct(user, product)
                .map(this::toResponse)
                .orElseGet(() -> {
                    FavoriteEntity favorite = FavoriteEntity.builder()
                            .user(user)
                            .product(product)
                            .build();

                    FavoriteEntity saved = favoriteRepository.save(favorite);
                    return toResponse(saved);
                });
    }

    @Override
    @Transactional
    public void deleteFavorite(String email, Long productId) {
        UserEntity user = getUserByEmail(email);
        ProductEntity product = getProductById(productId);

        FavoriteEntity favorite = favoriteRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new RuntimeException("Favorite product not found"));

        favoriteRepository.delete(favorite);
    }

    private UserEntity getUserByEmail(String email) {
        return shopUserResolverService.getOrCreateByEmail(email);
    }

    private ProductEntity getProductById(Long productId) {
        if (productId == null || productId <= 0) {
            throw new RuntimeException("Product not found");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    private FavoriteResponse toResponse(FavoriteEntity favorite) {
        ProductEntity product = favorite.getProduct();

        return FavoriteResponse.builder()
                .favoriteId(favorite.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .brand(product.getBrand())
                .gender(product.getGender())
                .concentration(product.getConcentration())
                .price(product.getPrice())
                .imageUrl(getMainProductImage(product))
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    private String getMainProductImage(ProductEntity product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages()
                .stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsMain()))
                .map(ProductImageEntity::getSecureUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElseGet(() -> product.getImages()
                        .stream()
                        .map(ProductImageEntity::getSecureUrl)
                        .filter(url -> url != null && !url.isBlank())
                        .findFirst()
                        .orElse(null));
    }
}