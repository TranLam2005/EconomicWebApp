package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartFavoriteDemoController {
    @GetMapping("/wishlist-demo")
    public String wishlistDemo() {
        return "pages/wishlist-demo";
    }

    @GetMapping("/cart-demo")
    public String cartDemo() {
        return "pages/cart-demo";
    }
}