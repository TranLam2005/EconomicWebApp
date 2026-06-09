package com.example.demo.controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.reponse.FavoriteResponse;
import com.example.demo.services.FavoriteService;

@RestController
@RequestMapping("/private/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<FavoriteResponse> getFavorites(Authentication authentication) {
        return favoriteService.getFavorites(authentication.getName());
    }

    @PostMapping("/{productId}")
    public FavoriteResponse addFavorite(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        return favoriteService.addFavorite(authentication.getName(), productId);
    }

    @DeleteMapping("/{productId}")
    public String deleteFavorite(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        favoriteService.deleteFavorite(authentication.getName(), productId);
        return "Deleted favorite successfully";
    }
}