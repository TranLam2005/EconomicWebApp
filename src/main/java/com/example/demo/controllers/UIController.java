package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pub")
public class UIController {
    @GetMapping("/upload-file")
    public String uploadFile(Model model) {
        return "pages/upload-file";
    }
}
