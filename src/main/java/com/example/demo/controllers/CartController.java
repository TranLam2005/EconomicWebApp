package com.example.demo.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.reponse.CartResponse;
import com.example.demo.dtos.request.CartAddRequest;
import com.example.demo.dtos.request.CartUpdateRequest;
import com.example.demo.services.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private static final String DEFAULT_DEMO_EMAIL = "dang@test.com";

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        return cartService.getCart(resolveEmail(authentication, email));
    }

    @PostMapping
    public CartResponse addToCart(
            @RequestBody @Valid CartAddRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        return cartService.addToCart(resolveEmail(authentication, email), request);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(
            @PathVariable Long itemId,
            @RequestBody @Valid CartUpdateRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        return cartService.updateItem(resolveEmail(authentication, email), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(
            @PathVariable Long itemId,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        return cartService.removeItem(resolveEmail(authentication, email), itemId);
    }

    @DeleteMapping
    public String clearCart(
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        cartService.clearCart(resolveEmail(authentication, email));
        return "Cleared cart successfully";
    }

    private String resolveEmail(Authentication authentication, String email) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }

        if (email != null && !email.isBlank()) {
            return email;
        }

        return DEFAULT_DEMO_EMAIL;
    }
}