package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/stock")
public class StockController {
    private final ProductService productService;

    public StockController(ProductService productService) {
        this.productService = productService;
    }

    private Integer getUserId(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null ? user.getId() : 0;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        model.addAttribute("products", productService.findAll(getUserId(session)));
        return "stock";
    }

    @GetMapping("/low-stock")
    @ResponseBody
    public List<com.inventory.entity.Product> lowStock(HttpSession session) {
        return productService.findLowStockProducts(getUserId(session));
    }
}