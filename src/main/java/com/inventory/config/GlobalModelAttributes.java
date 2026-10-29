package com.inventory.config;

import com.inventory.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("industryMode")
    public String getIndustryMode(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("currentUser");
            if (user != null && user.getIndustryMode() != null) {
                return user.getIndustryMode();
            }
        }
        return "mixed";
    }
}
