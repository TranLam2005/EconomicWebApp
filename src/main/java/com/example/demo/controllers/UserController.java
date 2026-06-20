package com.example.demo.controllers;
import com.example.demo.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class UserController {
    private UserService userService;
    private PasswordEncoder passwordEncoder;
}
