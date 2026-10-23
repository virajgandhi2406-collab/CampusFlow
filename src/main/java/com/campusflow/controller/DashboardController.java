package com.campusflow.controller;

import com.campusflow.dto.DashboardMetricsDto;
import com.campusflow.entity.User;
import com.campusflow.service.DashboardService;
import com.campusflow.service.SecurityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityService securityService;

    public DashboardController(DashboardService dashboardService, SecurityService securityService) {
        this.dashboardService = dashboardService;
        this.securityService = securityService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        User user = securityService.getCurrentUser();
        DashboardMetricsDto metrics = dashboardService.getDashboardMetrics(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("metrics", metrics);
        model.addAttribute("activeNav", "dashboard");

        return "dashboard/index";
    }
}
