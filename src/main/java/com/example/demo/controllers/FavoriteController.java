package com.example.demo.controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.reponse.FavoriteResponse;
import com.example.demo.services.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private static final String DEFAULT_DEMO_EMAIL = "dang@test.com";

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<FavoriteResponse> getFavorites(
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        return favoriteService.getFavorites(resolveEmail(authentication, email));
    }

    @PostMapping("/{productId}")
    public FavoriteResponse addFavorite(
            @PathVariable Long productId,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        return favoriteService.addFavorite(resolveEmail(authentication, email), productId);
    }

    @DeleteMapping("/{productId}")
    public String deleteFavorite(
            @PathVariable Long productId,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        favoriteService.deleteFavorite(resolveEmail(authentication, email), productId);
        return "Deleted favorite successfully";
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