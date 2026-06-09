package com.example.demo.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.reponse.CartResponse;
import com.example.demo.dtos.request.CartAddRequest;
import com.example.demo.dtos.request.CartUpdateRequest;
import com.example.demo.services.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/private/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        return cartService.getCart(authentication.getName());
    }

    @PostMapping
    public CartResponse addToCart(
            @RequestBody @Valid CartAddRequest request,
            Authentication authentication
    ) {
        return cartService.addToCart(authentication.getName(), request);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(
            @PathVariable Long itemId,
            @RequestBody @Valid CartUpdateRequest request,
            Authentication authentication
    ) {
        return cartService.updateItem(authentication.getName(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        return cartService.removeItem(authentication.getName(), itemId);
    }

    @DeleteMapping
    public String clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return "Cleared cart successfully";
    }
}