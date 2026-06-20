package com.example.demo.services;

import com.example.demo.entities.UserEntity;
import com.example.demo.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ShopUserResolverService {
    private static final String DEFAULT_EMAIL = "dang@test.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopUserResolverService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserEntity getOrCreateByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return userRepository.findFirstByEmailIgnoreCaseOrderByIdDesc(normalizedEmail)
                .orElseGet(() -> createDemoUser(normalizedEmail));
    }

    private UserEntity createDemoUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName("Demo");
        user.setLastName("User");
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode("123456"));
        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        return normalizedEmail.isBlank() ? DEFAULT_EMAIL : normalizedEmail;
    }
}
