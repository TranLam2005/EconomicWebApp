package com.example.demo.repositories;

import com.example.demo.entities.UserEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    @NullMarked
    List<UserEntity> findAll();

    @NullMarked
    Optional<UserEntity> findByEmail(String email);

    @NullMarked
    List<UserEntity> findAllByEmailIgnoreCase(String email);

    @NullMarked
    Optional<UserEntity> findFirstByEmailIgnoreCaseOrderByIdDesc(String email);

    boolean existsByEmailIgnoreCase(String email);
}
