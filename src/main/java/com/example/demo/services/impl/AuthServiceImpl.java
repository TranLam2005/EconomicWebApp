package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.RegisterResponse;
import com.example.demo.dtos.reponse.TokenResponse;
import com.example.demo.dtos.request.LoginRequest;
import com.example.demo.dtos.request.RegisterRequest;
import com.example.demo.entities.UserEntity;
import com.example.demo.services.AuthService;
import com.example.demo.services.UserService;
import com.example.demo.util.JWTService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private UserService userService;

  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JWTService jwtService;

  public TokenResponse login(LoginRequest request) {
    Authentication authentication =  authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
    );
    UserDetails user = (UserDetails) authentication.getPrincipal();
    assert user != null;
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    return new TokenResponse(accessToken, refreshToken);
  }

  public TokenResponse refresh(String refreshToken) {
    String username = jwtService.extractUsername(refreshToken);
    UserDetails user = userDetailsService.loadUserByUsername(username);

    if (!jwtService.isTokenValid(refreshToken, user)) {
      throw new BadCredentialsException("Invalid refresh token");
    }

    String newAccessToken = jwtService.generateAccessToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    return new TokenResponse(newAccessToken, newRefreshToken);
  }

  public RegisterResponse register(RegisterRequest request) {
    UserEntity user = new UserEntity();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    userService.addUser(user);
    return new RegisterResponse(user.getFirstName(), user.getLastName(),  user.getEmail());
  }
}
