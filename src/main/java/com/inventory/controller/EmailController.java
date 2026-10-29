package com.inventory.controller;

import com.inventory.dao.UserRepository;
import com.inventory.service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    public EmailController(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestParam String email) {
        try {
            emailService.sendVerificationCode(email);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @PostMapping("/send-reset-code")
    public Map<String, Object> sendResetCode(@RequestParam String email) {
        if (!userRepository.findByEmail(email).isPresent()) {
            return Map.of("success", false, "error", "该邮箱未注册");
        }
        try {
            emailService.sendVerificationCode(email);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public Map<String, Object> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean ok = emailService.verifyCode(email, code);
        return Map.of("success", ok, "error", ok ? null : "验证码错误或已过期");
    }
}
