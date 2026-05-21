package com.saftyhub.project1.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session) {
        String role = (String) session.getAttribute("rulePosition");
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("userProfilePic", session.getAttribute("userProfilePic"));
        model.addAttribute("userGender", session.getAttribute("userGender"));
        model.addAttribute("isManager", "Manager".equalsIgnoreCase(role));
        model.addAttribute("isAdmin", "Admin".equalsIgnoreCase(role));
    }
}
