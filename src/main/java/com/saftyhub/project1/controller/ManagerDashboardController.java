package com.saftyhub.project1.controller;

import com.saftyhub.project1.dto.EmployeeDto;
import com.saftyhub.project1.services.EmployeeService;
import com.saftyhub.project1.services.ManagerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final EmployeeService employeeService;
    private final ManagerService managerService;

    @GetMapping
    public String managerDashboard(
            @RequestParam(defaultValue = "en")       String lang,
            @RequestParam(defaultValue = "dark")     String theme,
            @RequestParam(defaultValue = "overview") String tab,
            Model model, HttpSession session) {

        if (session.getAttribute("accountId") == null) return "redirect:/";
        String role = (String) session.getAttribute("rulePosition");
        if (!"Manager".equalsIgnoreCase(role))
            return "redirect:/dashboard?lang=" + lang + "&theme=" + theme;

        EmployeeDto.Stats stats = employeeService.getStats();
        model.addAttribute("stats", stats);
        model.addAttribute("deptChartLabels", List.of());
        model.addAttribute("deptChartScores", List.of());
        model.addAttribute("deptStats",       List.of());

        // Always preload data used by the tabbed UI on the page.
        // Otherwise, switching tabs client-side won't have DB-backed data.
        var users = managerService.getAllUsers();
        model.addAttribute("users",       users);
        model.addAttribute("departments", managerService.getAllDepartments());
        model.addAttribute("jobs",        managerService.getAllJobs());
        model.addAttribute("roles",       managerService.getAllRoles());

        // `account_info` keeps the email, not `Users`.
        Map<Integer, String> userEmails = new LinkedHashMap<>();
        for (var u : users) {
            if (u == null || u.getUserId() == null) continue;
            managerService.getAccountByUserId(u.getUserId())
                    .ifPresent(a -> userEmails.put(u.getUserId(), a.getAccountEmail()));
        }
        model.addAttribute("userEmails", userEmails);

        switch (tab.toLowerCase()) {
            default -> {
                List<EmployeeDto.DeptStat> deptStats = employeeService.getDeptStats();
                List<String> chartLabels = deptStats.stream()
                        .map(d -> "ar".equals(lang) ? d.getNameAr() : d.getName())
                        .collect(Collectors.toList());
                List<Double> chartScores = deptStats.stream()
                        .map(d -> Math.round(d.getOverallScore() * 10.0) / 10.0)
                        .collect(Collectors.toList());
                model.addAttribute("deptStats",       deptStats);
                model.addAttribute("deptChartLabels", chartLabels);
                model.addAttribute("deptChartScores", chartScores);
            }
        }

        model.addAttribute("lang",            lang);
        model.addAttribute("theme",           theme);
        model.addAttribute("tab",             tab);
        model.addAttribute("rulePosition",    role);
        model.addAttribute("profileInitials", initials((String) session.getAttribute("username")));
        model.addAttribute("activePage",      "manager-dashboard");
        return "pages/manager-dashboard";
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
