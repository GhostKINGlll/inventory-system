package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, Model model) {
        try {
            User user = userService.login(username, password);
            String token = UUID.randomUUID().toString().replace("-", "");
            userService.updateSessionToken(user.getId(), token);
            session.setAttribute("currentUser", user);
            session.setAttribute("sessionToken", token);
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String email, @RequestParam String code, Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次密码不一致");
            return "register";
        }
        try {
            userService.register(username, password, email, code);
            model.addAttribute("success", "注册成功，请登录");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            userService.updateSessionToken(user.getId(), null);
        }
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, @RequestParam String code,
                                  @RequestParam String password, @RequestParam String confirmPassword,
                                  Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次密码不一致");
            return "forgot-password";
        }
        try {
            userService.resetPassword(email, code, password);
            model.addAttribute("success", "密码重置成功，请登录");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }
}