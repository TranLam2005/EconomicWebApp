package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LongPageController {
    @GetMapping({"/cart", "/gio-hang", "/long/cart", "/long/gio-hang"})
    public String cartPage() {
        return "pages/long-cart";
    }

    @GetMapping({"/wishlist", "/favorites", "/yeu-thich", "/long/wishlist", "/long/yeu-thich"})
    public String wishlistPage() {
        return "pages/long-wishlist";
    }

    @GetMapping({"/search", "/tim-kiem", "/long/search", "/long/tim-kiem"})
    public String searchPage() {
        return "pages/long-search";
    }
}
