package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/report")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    private Integer getUserId(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null ? user.getId() : 0;
    }

    @GetMapping
    public String report(@RequestParam(required = false) LocalDate startDate,
                         @RequestParam(required = false) LocalDate endDate,
                         @RequestParam(defaultValue = "day") String groupBy,
                         HttpSession session, Model model) {
        Integer userId = getUserId(session);

        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Map<String, Object>> stats = reportService.getProfitStats(startDate, endDate, userId, groupBy);
        Map<String, Object> summary = reportService.getSummary(startDate, endDate, userId);

        model.addAttribute("stats", stats);
        model.addAttribute("summary", summary);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("groupBy", groupBy);

        return "report";
    }
}
