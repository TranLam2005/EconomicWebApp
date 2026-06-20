package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.reponse.AdminUserResponse;
import com.example.demo.dtos.request.AdminUserCreateRequest;
import com.example.demo.dtos.request.AdminUserUpdateRequest;
import com.example.demo.services.AdminUserManagementService;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final AdminUserManagementService adminUserManagementService;

    public AdminUserController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserManagementService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserManagementService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(@RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserManagementService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(adminUserManagementService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminUserManagementService.deleteUser(id);
        return ResponseEntity.ok("Da xoa user id = " + id);
    }
}
