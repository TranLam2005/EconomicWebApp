package com.example.demo.services;

import java.util.List;

import com.example.demo.dtos.reponse.AdminUserResponse;
import com.example.demo.dtos.request.AdminUserCreateRequest;
import com.example.demo.dtos.request.AdminUserUpdateRequest;

public interface AdminUserManagementService {
    List<AdminUserResponse> getAllUsers();

    AdminUserResponse getUserById(Long id);

    AdminUserResponse createUser(AdminUserCreateRequest request);

    AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request);

    void deleteUser(Long id);
}
