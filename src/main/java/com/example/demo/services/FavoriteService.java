package com.example.demo.services;

import java.util.List;

import com.example.demo.dtos.reponse.FavoriteResponse;

public interface FavoriteService {
    List<FavoriteResponse> getFavorites(String email);

    FavoriteResponse addFavorite(String email, Long productId);

    void deleteFavorite(String email, Long productId);
}