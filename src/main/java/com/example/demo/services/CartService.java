package com.example.demo.services;

import com.example.demo.dtos.reponse.CartResponse;
import com.example.demo.dtos.request.CartAddRequest;
import com.example.demo.dtos.request.CartUpdateRequest;

public interface CartService {
    CartResponse getCart(String email);

    CartResponse addToCart(String email, CartAddRequest request);

    CartResponse updateItem(String email, Long itemId, CartUpdateRequest request);

    CartResponse removeItem(String email, Long itemId);

    void clearCart(String email);
}