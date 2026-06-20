package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.RegisterResponse;
import com.example.demo.dtos.reponse.TokenResponse;
import com.example.demo.dtos.request.LoginRequest;
import com.example.demo.dtos.request.RegisterRequest;
import com.example.demo.entities.UserEntity;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AuthService;
import com.example.demo.services.UserService;
import com.example.demo.util.JWTService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private UserService userService;
  @Autowired
  private UserRepository userRepository;

  private final UserDetailsService userDetailsService;
  private final JWTService jwtService;

  @Override
  @Transactional
  public TokenResponse login(LoginRequest request) {
    String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase(Locale.ROOT);
    String rawPassword = request.getPassword() == null ? "" : request.getPassword();

    if (email.isBlank() || rawPassword.isBlank()) {
      throw new BadCredentialsException("Email hoặc mật khẩu không đúng");
    }

    List<UserEntity> candidates = userRepository.findAllByEmailIgnoreCase(email);
    if (candidates == null || candidates.isEmpty()) {
      throw new BadCredentialsException("Email hoặc mật khẩu không đúng");
    }

    // Nếu trước đó lỡ đăng ký trùng email, thử tất cả bản ghi của email đó và ưu tiên bản ghi mới nhất.
    UserEntity matchedUser = candidates.stream()
            .sorted(Comparator.comparing(UserEntity::getId, Comparator.nullsLast(Comparator.reverseOrder())))
            .filter(user -> passwordMatches(rawPassword, user.getPassword()))
            .findFirst()
            .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không đúng"));

    // Nếu dữ liệu cũ đang lưu mật khẩu dạng thường, tự mã hóa lại sau khi đăng nhập đúng.
    if (!isBcryptHash(matchedUser.getPassword())) {
      matchedUser.setPassword(passwordEncoder.encode(rawPassword));
      matchedUser = userRepository.save(matchedUser);
    }

    UserDetails userDetails = User.withUsername(matchedUser.getEmail())
            .password(matchedUser.getPassword())
            .authorities(new SimpleGrantedAuthority(normalizeAuthority(matchedUser.getRole())))
            .build();

    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    return new TokenResponse(accessToken, refreshToken);
  }

  @Override
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

  @Override
  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase(Locale.ROOT);
    if (email.isBlank()) {
      throw new IllegalArgumentException("Email không được để trống");
    }

    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new IllegalArgumentException("Email đã tồn tại");
    }

    String rawPassword = request.getPassword() == null ? "" : request.getPassword();
    if (rawPassword.isBlank()) {
      throw new IllegalArgumentException("Mật khẩu không được để trống");
    }

    UserEntity user = new UserEntity();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setFirstName(request.getFirstName() == null ? "" : request.getFirstName().trim());
    user.setLastName(request.getLastName() == null ? "" : request.getLastName().trim());
    user.setRole("USER");

    userService.addUser(user);
    return new RegisterResponse(user.getFirstName(), user.getLastName(), user.getEmail());
  }

  private boolean passwordMatches(String rawPassword, String storedPassword) {
    if (storedPassword == null || storedPassword.isBlank()) {
      return false;
    }
    if (isBcryptHash(storedPassword)) {
      return passwordEncoder.matches(rawPassword, storedPassword);
    }
    // Hỗ trợ dữ liệu cũ từng bị lưu plain text trong DB khi test.
    return rawPassword.equals(storedPassword);
  }

  private boolean isBcryptHash(String password) {
    return password != null && (
            password.startsWith("$2a$")
                    || password.startsWith("$2b$")
                    || password.startsWith("$2y$")
    );
  }

  private String normalizeAuthority(String role) {
    String normalizedRole = role == null || role.isBlank()
            ? "USER"
            : role.trim().toUpperCase(Locale.ROOT);
    return normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole;
  }
}
