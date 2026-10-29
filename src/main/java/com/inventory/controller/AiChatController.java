package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.service.AiChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping("/chat")
    public String chatPage(Model model) {
        return "ai-chat";
    }

    @PostMapping("/ask")
    @ResponseBody
    public Map<String, Object> ask(@RequestBody Map<String, String> request, HttpSession session) {
        String message = request.get("message");
        User user = (User) session.getAttribute("currentUser");
        Integer userId = user != null ? user.getId() : 0;
        return aiChatService.chat(message, userId);
    }
}