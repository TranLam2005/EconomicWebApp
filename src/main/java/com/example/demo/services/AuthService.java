package com.example.demo.services;

import com.example.demo.dtos.reponse.RegisterResponse;
import com.example.demo.dtos.reponse.TokenResponse;
import com.example.demo.dtos.request.LoginRequest;
import com.example.demo.dtos.request.RegisterRequest;

public interface AuthService {
    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);

    RegisterResponse register(RegisterRequest request);
}
