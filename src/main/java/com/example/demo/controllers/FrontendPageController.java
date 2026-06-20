package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FrontendPageController {

    @GetMapping({"/", "/home", "/shop", "/shop/home"})
    public String home() {
        return "pages/frontend/home";
    }

    @GetMapping({"/cart", "/shop/cart"})
    public String cart() {
        return "pages/frontend/cart";
    }

    @GetMapping({"/favorites", "/wishlist", "/shop/favorites"})
    public String favorites() {
        return "pages/frontend/favorites";
    }

    @GetMapping({"/account", "/shop/account"})
    public String account() {
        return "pages/frontend/account";
    }

    @GetMapping({"/search", "/shop/search"})
    public String search() {
        return "pages/frontend/search";
    }

    @GetMapping({"/product-detail", "/shop/product-detail"})
    public String productDetailWithoutId(Model model) {
        model.addAttribute("productId", 1);
        return "pages/frontend/product-detail";
    }

    @GetMapping({"/product-detail/{id}", "/shop/products/{id}"})
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "pages/frontend/product-detail";
    }

    @GetMapping({"/customers", "/customer-management", "/shop/customers"})
    public String customerManagement() {
        return "pages/frontend/customer-management";
    }
}
