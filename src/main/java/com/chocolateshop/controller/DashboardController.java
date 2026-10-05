package com.chocolateshop.controller;

import com.chocolateshop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the business KPI dashboard.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardService.DashboardData data = dashboardService.getDashboardData();
        model.addAttribute("data", data);
        model.addAttribute("activeNav", "dashboard");
        return "dashboard";
    }
}
