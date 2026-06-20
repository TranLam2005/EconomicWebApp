package com.example.demo.services;

import com.example.demo.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserEntity> findAll();

    Optional<UserEntity> findByEmail(String email);

    void addUser(UserEntity user);
}
