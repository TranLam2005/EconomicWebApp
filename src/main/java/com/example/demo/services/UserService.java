package com.example.demo.services;

import com.example.demo.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserEntity> findAll();

    // load user by user name
    Optional<UserEntity> findByEmail(String username);

    // save user into db
    void addUser(UserEntity user);
}
