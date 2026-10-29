package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.dao.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SettingsController {
    private final UserRepository userRepository;

    public SettingsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/api/settings/industry-mode")
    @ResponseBody
    public Map<String, Object> setIndustryMode(@RequestParam String mode, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            user.setIndustryMode(mode);
            userRepository.save(user);
            session.setAttribute("currentUser", user);
            result.put("success", true);
            result.put("mode", mode);
        } else {
            result.put("success", false);
            result.put("message", "未登录");
        }
        return result;
    }
}
