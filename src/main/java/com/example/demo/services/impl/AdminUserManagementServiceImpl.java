package com.example.demo.services.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.reponse.AdminUserResponse;
import com.example.demo.dtos.request.AdminUserCreateRequest;
import com.example.demo.dtos.request.AdminUserUpdateRequest;
import com.example.demo.entities.UserEntity;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AdminUserManagementService;

@Service
public class AdminUserManagementServiceImpl implements AdminUserManagementService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserManagementServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(AdminUserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        UserEntity user = findUserOrThrow(id);
        return AdminUserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public AdminUserResponse createUser(AdminUserCreateRequest request) {
        validateCreateRequest(request);
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        UserEntity user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(normalizeRole(request.getRole()));

        return AdminUserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        UserEntity user = findUserOrThrow(id);

        if (hasText(request.getEmail()) && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        throw new IllegalArgumentException("Email already exists");
                    });
            user.setEmail(request.getEmail());
        }

        if (hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (hasText(request.getRole())) {
            user.setRole(normalizeRole(request.getRole()));
        }

        return AdminUserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    private UserEntity findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id = " + id));
    }

    private void validateCreateRequest(AdminUserCreateRequest request) {
        if (!hasText(request.getFirstName())) {
            throw new IllegalArgumentException("First name is required");
        }
        if (!hasText(request.getLastName())) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (!hasText(request.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private String normalizeRole(String role) {
        if (!hasText(role)) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        if (!normalized.equals("ADMIN") && !normalized.equals("USER")) {
            throw new IllegalArgumentException("Role must be USER or ADMIN");
        }

        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
