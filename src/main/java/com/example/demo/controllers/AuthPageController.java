package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping({"/dang-nhap", "/login"})
    public String loginPage() {
        return "pages/auth-login";
    }

    @GetMapping({"/dang-ky", "/register"})
    public String registerPage() {
        return "pages/auth-register";
    }
}
