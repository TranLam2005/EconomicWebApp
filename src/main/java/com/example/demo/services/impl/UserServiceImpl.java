package com.example.demo.services.impl;

import com.example.demo.entities.UserEntity;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
  @Autowired
  private UserRepository userRepository;

  @Override
  public List<UserEntity> findAll() {
    return userRepository.findAll();
  }

  @Override
  public Optional<UserEntity> findByEmail(String email) {
    String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    if (normalizedEmail.isBlank()) {
      return Optional.empty();
    }
    // Nếu DB đang bị trùng email do các lần đăng ký lỗi trước đó, ưu tiên bản ghi mới nhất.
    return userRepository.findFirstByEmailIgnoreCaseOrderByIdDesc(normalizedEmail);
  }

  @Override
  public void addUser(UserEntity user) {
    userRepository.save(user);
  }
}
