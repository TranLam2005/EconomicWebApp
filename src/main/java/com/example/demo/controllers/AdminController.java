package com.example.demo.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/public/admin")
@Controller
@RequiredArgsConstructor
public class AdminController {
  @GetMapping("/dashboard")
  public String dashboard() {
    return "pages/admin/dashboard";
  }

  @GetMapping("/create-product")
  public String createProduct (Model model) {
    return "pages/createProduct";
  }
}
