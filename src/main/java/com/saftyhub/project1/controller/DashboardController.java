package com.saftyhub.project1.controller;

import com.saftyhub.project1.dto.EmployeeDto;
import com.saftyhub.project1.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeService employeeService;

    // ─── Helper ───────────────────────────────────────────────────────────────
    private boolean isManager(HttpSession s) {
        return "Manager".equalsIgnoreCase((String) s.getAttribute("rulePosition"));
    }

    private void addCommon(Model m, HttpSession s, String lang, String theme, String page) {
        m.addAttribute("lang",         lang);
        m.addAttribute("theme",        theme);
        m.addAttribute("rulePosition", s.getAttribute("rulePosition"));
        m.addAttribute("profileInitials", initials((String) s.getAttribute("username")));
        m.addAttribute("userGender",   s.getAttribute("userGender"));
        m.addAttribute("userProfilePic", s.getAttribute("userProfilePic"));
        m.addAttribute("activePage",   page);
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // ─── Root redirects to appropriate page ──────────────────────────────────
    // @GetMapping("/")
    // public String root(@RequestParam(defaultValue="en") String lang,
    //                    @RequestParam(defaultValue="dark") String theme,
    //                    HttpSession session) {
    //     if (session.getAttribute("accountId") == null) return "Login";
    //     if (isManager(session)) return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme;
    //     return "redirect:/dashboard?lang=" + lang + "&theme=" + theme;
    // }

    // ─── Employee Dashboard ───────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue="") String query,
            @RequestParam(defaultValue="ALL") String department,
            @RequestParam(defaultValue="ALL") String status,
            @RequestParam(defaultValue="en") String lang,
            @RequestParam(defaultValue="dark") String theme,
            Model model, HttpSession session) {

        if (session.getAttribute("accountId") == null) return "redirect:/";
        // Managers belong on /manager/dashboard
        if (isManager(session)) return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme;

        EmployeeDto.Stats stats = employeeService.getStats();
        List<EmployeeDto.Summary> employees = employeeService.searchAndFilter(query, department, status);

        model.addAttribute("stats",      stats);
        model.addAttribute("employees",  employees);
        model.addAttribute("query",      query);
        model.addAttribute("department", department);
        model.addAttribute("status",     status);
        addCommon(model, session, lang, theme, "dashboard");
        return "pages/dashboard";
    }

    // ─── Employees Page ───────────────────────────────────────────────────────
    @GetMapping("/employees")
    public String employees(
            @RequestParam(defaultValue="") String query,
            @RequestParam(defaultValue="ALL") String department,
            @RequestParam(defaultValue="ALL") String status,
            @RequestParam(defaultValue="en") String lang,
            @RequestParam(defaultValue="dark") String theme,
            Model model, HttpSession session) {

        if (session.getAttribute("accountId") == null) return "redirect:/";
        // Managers should use the Manager Panel employees view
        if (isManager(session)) return "redirect:/manager?lang=" + lang + "&theme=" + theme;
        List<EmployeeDto.Summary> employees = employeeService.searchAndFilter(query, department, status);
        long activeCount = employees.stream().filter(e -> e.getStatus() == EmployeeDto.StatusEnum.ACTIVE).count();
        long procrastCount = employees.stream().filter(e -> e.getStatus() == EmployeeDto.StatusEnum.PROCRASTINATOR).count();
        long completedCount = employees.stream().filter(e -> e.getStatus() == EmployeeDto.StatusEnum.COMPLETED).count();
        model.addAttribute("employees",  employees);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("procrastCount", procrastCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("query",      query);
        model.addAttribute("department", department);
        model.addAttribute("status",     status);
        addCommon(model, session, lang, theme, "employees");
        return "pages/employees";
    }

    // ─── Courses Page ─────────────────────────────────────────────────────────
    @GetMapping("/courses")
    public String courses(@RequestParam(defaultValue="en") String lang,
                          @RequestParam(defaultValue="dark") String theme,
                          Model model, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        addCommon(model, session, lang, theme, "courses");
        return "pages/courses";
    }

    // ─── Games Page ───────────────────────────────────────────────────────────
    @GetMapping("/games")
    public String games(@RequestParam(defaultValue="en") String lang,
                        @RequestParam(defaultValue="dark") String theme,
                        Model model, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        addCommon(model, session, lang, theme, "games");
        return "pages/games";
    }

    // ─── Achievements Page ───────────────────────────────────────────────────────────
    @GetMapping("/achievements")
    public String achievements(@RequestParam(defaultValue="en") String lang,
                        @RequestParam(defaultValue="dark") String theme,
                        Model model, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        addCommon(model, session, lang, theme, "achievements");
        return "pages/achievements";
    }

    // ─── Reports Page ────────────────────────────────────────────────────────
    @GetMapping("/reports")
    public String reports(@RequestParam(defaultValue="en") String lang,
                          @RequestParam(defaultValue="dark") String theme,
                          Model model, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        EmployeeDto.Stats stats = employeeService.getStats();
        model.addAttribute("stats", stats);
        addCommon(model, session, lang, theme, "reports");
        return "pages/reports";
    }

    // ─── Employee Detail ──────────────────────────────────────────────────────
    @GetMapping("/employees/{id}/detail")
    public String employeeDetail(@PathVariable Integer id,
                                 @RequestParam(defaultValue="en") String lang,
                                 @RequestParam(defaultValue="dark") String theme,
                                 Model model, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        EmployeeDto.Detail detail = employeeService.getDetail(id);
        model.addAttribute("employee", detail);
        model.addAttribute("lang",  lang);
        model.addAttribute("theme", theme);
        return "pages/employee-detail";
    }

    // ─── Reset Progress (stub — no real data yet) ────────────────────────────
    @PostMapping("/employees/{id}/reset-progress")
    public String resetProgress(@PathVariable Integer id,
                                @RequestParam(defaultValue="en") String lang,
                                @RequestParam(defaultValue="dark") String theme,
                                RedirectAttributes ra, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        ra.addFlashAttribute("toast", "Progress reset successfully");
        return "redirect:/employees?lang=" + lang + "&theme=" + theme;
    }

    // ─── Update Deadline (stub) ───────────────────────────────────────────────
    @PostMapping("/employees/{id}/update-deadline")
    public String updateDeadline(@PathVariable Integer id,
                                 @RequestParam(required=false) String deadline,
                                 @RequestParam(defaultValue="en") String lang,
                                 @RequestParam(defaultValue="dark") String theme,
                                 RedirectAttributes ra, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        ra.addFlashAttribute("toast", "Deadline updated");
        return "redirect:/employees?lang=" + lang + "&theme=" + theme;
    }

    // ─── Drop Course (stub) ───────────────────────────────────────────────────
    @PostMapping("/employees/{id}/drop-course/{courseId}")
    public String dropCourse(@PathVariable Integer id, @PathVariable Integer courseId,
                             @RequestParam(defaultValue="en") String lang,
                             @RequestParam(defaultValue="dark") String theme,
                             RedirectAttributes ra, HttpSession session) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        ra.addFlashAttribute("toast", "Course dropped");
        return "redirect:/employees?lang=" + lang + "&theme=" + theme;
    }
}
