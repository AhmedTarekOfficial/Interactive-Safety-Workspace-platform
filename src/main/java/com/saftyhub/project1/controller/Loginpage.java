package com.saftyhub.project1.controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import com.saftyhub.project1.model.Account_information;
import com.saftyhub.project1.model.Users;
import com.saftyhub.project1.model.Rules;
import com.saftyhub.project1.repository.*;

@Controller
public class Loginpage {

    @Autowired private UserRepository    rs;
    @Autowired private AccountRepository ac;
    @Autowired private RulesRepository   rulesRepo;

    // ── GET / ──────────────────────────────────────────────────────────────────
    @GetMapping("/")
    public String showLoginPage(
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            HttpSession session, Model model) {

        // Already logged in? route by role
        if (session.getAttribute("accountId") != null) {
            String role = (String) session.getAttribute("rulePosition");
            String q = "?lang=" + lang + "&theme=" + theme;
            if ("Manager".equalsIgnoreCase(role)) return "redirect:/manager/dashboard" + q;
            if ("Admin".equalsIgnoreCase(role))   return "redirect:/admin" + q;
            return "redirect:/index" + q;
        }
        model.addAttribute("lang",  lang);
        model.addAttribute("theme", theme);
        return "Login";
    }

    // ── GET /login (force show login) ─────────────────────────────────────────
    // This route is useful when you explicitly want to see the login page,
    // even if a previous session still exists in the browser.
    @GetMapping("/login")
    public String showLoginPageForced(
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            Model model) {
        model.addAttribute("lang",  lang);
        model.addAttribute("theme", theme);
        return "Login";
    }

    // ── POST /login ────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam("email")    String email,
            @RequestParam("password") String pass,
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            HttpSession session, Model model) {

        Optional<Account_information> accountOpt = ac.findByAccountEmail(email);
        if (accountOpt.isEmpty()) {
            model.addAttribute("error", lang.equals("ar") ? "بيانات الدخول غير صحيحة" : "Invalid login credentials");
            model.addAttribute("lang", lang); model.addAttribute("theme", theme);
            return "Login";
        }
        Account_information account = accountOpt.get();

        Optional<Users> userOpt = rs.findById(account.getAccountId());
        if (userOpt.isEmpty()) {
            model.addAttribute("error", lang.equals("ar") ? "بيانات الدخول غير صحيحة" : "Invalid login credentials");
            model.addAttribute("lang", lang); model.addAttribute("theme", theme);
            return "Login";
        }
        if (!account.getAccountPassword().equals(pass)) {
            model.addAttribute("error", lang.equals("ar") ? "كلمة المرور غير صحيحة" : "Incorrect password");
            model.addAttribute("lang", lang); model.addAttribute("theme", theme);
            return "Login";
        }

        Users user = userOpt.get();
        String roleName = "User";
        if (user.getRule() != null && user.getRule().getName() != null) {
            roleName = user.getRule().getName();
        }

        session.setAttribute("accountId",    account.getAccountId());
        session.setAttribute("rulePosition", roleName);
        session.setAttribute("username",     user.getUsername());
        session.setAttribute("userGender",   user.getGender());
        session.setAttribute("userProfilePic", user.getProfilePicture());

        String q = "?lang=" + lang + "&theme=" + theme;
        if ("Manager".equalsIgnoreCase(roleName)) return "redirect:/manager/dashboard" + q;
        if ("Admin".equalsIgnoreCase(roleName))   return "redirect:/admin" + q;
        return "redirect:/index" + q;
    }

    // ── GET /index (legacy — redirect to /dashboard) ──────────────────────────
    @GetMapping("/index")
    public String showDashboard(
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            HttpSession session,
            Model model) {
        if (session.getAttribute("accountId") == null) return "redirect:/";
        String role = (String) session.getAttribute("rulePosition");
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("isManager", "Manager".equalsIgnoreCase(role));
        model.addAttribute("isAdmin",   "Admin".equalsIgnoreCase(role));
        model.addAttribute("lang", lang);
        model.addAttribute("theme", theme);
        model.addAttribute("userProfilePic", session.getAttribute("userProfilePic"));
        model.addAttribute("userGender", session.getAttribute("userGender"));
        return "index";
    }

    // ── GET /logout ────────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
