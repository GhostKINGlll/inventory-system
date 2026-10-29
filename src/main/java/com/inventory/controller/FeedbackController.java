package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FeedbackController {

    private final EmailService emailService;

    public FeedbackController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/feedback")
    public String feedbackPage() {
        return "feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@RequestParam String subject, @RequestParam String message,
                                  HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            emailService.sendFeedback(user.getEmail(), user.getUsername(), subject, message);
            model.addAttribute("success", "反馈已发送，感谢您的建议！");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "feedback";
    }
}
