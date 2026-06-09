package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.CartItemResponse;
import com.example.demo.dtos.reponse.CartResponse;
import com.example.demo.dtos.request.CartAddRequest;
import com.example.demo.dtos.request.CartUpdateRequest;
import com.example.demo.entities.CartItemEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImage;
import com.example.demo.entities.ProductVariant;
import com.example.demo.entities.UserEntity;
import com.example.demo.repositories.CartItemRepository;
import com.example.demo.repositories.ProductVariantRepository;
import com.example.demo.services.CartService;
import com.example.demo.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserService userService;

    public CartServiceImpl(
            CartItemRepository cartItemRepository,
            ProductVariantRepository productVariantRepository,
            UserService userService
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        UserEntity user = getUserByEmail(email);
        List<CartItemEntity> items = cartItemRepository.findByUserOrderByCreatedAtAsc(user);

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        Integer totalQuantity = itemResponses.stream()
                .map(CartItemResponse::getQuantity)
                .reduce(0, Integer::sum);

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    @Transactional
    public CartResponse addToCart(String email, CartAddRequest request) {
        UserEntity user = getUserByEmail(email);
        ProductVariant variant = getVariantById(request.getVariantId());

        validateVariantCanBuy(variant);

        Integer quantityToAdd = request.getQuantity();

        CartItemEntity item = cartItemRepository.findByUserAndVariant(user, variant)
                .orElseGet(() -> CartItemEntity.builder()
                        .user(user)
                        .variant(variant)
                        .quantity(0)
                        .build());

        Integer newQuantity = item.getQuantity() + quantityToAdd;
        validateQuantity(variant, newQuantity);

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCart(email);
    }

    @Override
    @Transactional
    public CartResponse updateItem(String email, Long itemId, CartUpdateRequest request) {
        CartItemEntity item = getCartItemOfUser(email, itemId);

        ProductVariant variant = item.getVariant();
        validateVariantCanBuy(variant);
        validateQuantity(variant, request.getQuantity());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return getCart(email);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String email, Long itemId) {
        CartItemEntity item = getCartItemOfUser(email, itemId);
        cartItemRepository.delete(item);

        return getCart(email);
    }

    @Override
    @Transactional
    public void clearCart(String email) {
        UserEntity user = getUserByEmail(email);
        List<CartItemEntity> items = cartItemRepository.findByUserOrderByCreatedAtAsc(user);
        cartItemRepository.deleteAll(items);
    }

    private UserEntity getUserByEmail(String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ProductVariant getVariantById(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));
    }

    private CartItemEntity getCartItemOfUser(String email, Long itemId) {
        UserEntity user = getUserByEmail(email);

        CartItemEntity item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!Objects.equals(item.getUser().getId(), user.getId())) {
            throw new RuntimeException("You do not have permission to access this cart item");
        }

        return item;
    }

    private void validateVariantCanBuy(ProductVariant variant) {
        if (Boolean.FALSE.equals(variant.getIsActive())) {
            throw new RuntimeException("Product variant is inactive");
        }
    }

    private void validateQuantity(ProductVariant variant, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Integer stockQuantity = variant.getStockQuantity();
        if (stockQuantity != null && quantity > stockQuantity) {
            throw new RuntimeException("Not enough stock");
        }
    }

    private CartItemResponse toItemResponse(CartItemEntity item) {
        ProductVariant variant = item.getVariant();
        ProductEntity product = variant.getProduct();

        BigDecimal unitPrice = variant.getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .itemId(item.getId())
                .variantId(variant.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .brand(product.getBrand())
                .variantName(variant.getVariantName())
                .volumeMl(variant.getVolumeMl())
                .quantity(item.getQuantity())
                .stockQuantity(variant.getStockQuantity())
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .imageUrl(getImageUrl(variant, product))
                .build();
    }

    private String getImageUrl(ProductVariant variant, ProductEntity product) {
        String variantImage = getMainImageFromList(variant.getImages());

        if (variantImage != null) {
            return variantImage;
        }

        return getMainImageFromList(product.getImages());
    }

    private String getMainImageFromList(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsMain()))
                .map(ProductImage::getSecureUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElseGet(() -> images.stream()
                        .map(ProductImage::getSecureUrl)
                        .filter(url -> url != null && !url.isBlank())
                        .findFirst()
                        .orElse(null));
    }
}