package com.campusflow.controller;

import com.campusflow.dto.WeeklyStatsDto;
import com.campusflow.entity.User;
import com.campusflow.service.DashboardService;
import com.campusflow.service.SecurityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InsightsController {

    private final DashboardService dashboardService;
    private final SecurityService securityService;

    public InsightsController(DashboardService dashboardService, SecurityService securityService) {
        this.dashboardService = dashboardService;
        this.securityService = securityService;
    }

    @GetMapping("/insights")
    public String showProductivityInsights(Model model) {
        User user = securityService.getCurrentUser();
        WeeklyStatsDto stats = dashboardService.getWeeklyProductivityStats(user.getId());

        model.addAttribute("stats", stats);
        model.addAttribute("user", user);
        model.addAttribute("activeNav", "insights");

        return "insights/index";
    }
}
