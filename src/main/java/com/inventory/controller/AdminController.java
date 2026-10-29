package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user != null) {
            userService.setVip(id, !user.getVip());
        }
        return "redirect:/admin/users";
    }
}