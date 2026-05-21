package com.saftyhub.project1.controller;

import com.saftyhub.project1.dto.EmployeeDto;
import com.saftyhub.project1.services.EmployeeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.*;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final EmployeeService employeeService;

    @GetMapping("/profile")
    public String profile(@RequestParam(defaultValue = "en") String lang,
                          @RequestParam(defaultValue="dark") String theme,
                          Model model,
                          HttpSession session) {
        Object accountIdObj = session.getAttribute("accountId");
        if (accountIdObj == null) return "redirect:/";

        Integer accountId;
        if (accountIdObj instanceof Integer i) accountId = i;
        else accountId = Integer.valueOf(accountIdObj.toString());

        EmployeeDto.Detail me = employeeService.getDetail(accountId);
        String role = (String) session.getAttribute("rulePosition");

        model.addAttribute("me", me);
        model.addAttribute("lang", lang);
        model.addAttribute("theme", theme);
        model.addAttribute("rulePosition", role);
        model.addAttribute("profileInitials", initials((String) session.getAttribute("username")));
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("isManager", "Manager".equalsIgnoreCase(role));
        model.addAttribute("isAdmin", "Admin".equalsIgnoreCase(role));
        model.addAttribute("activePage", "profile");
        return "pages/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam String email,
                                @RequestParam(required = false) String gender,
                                @RequestParam(defaultValue = "en") String lang,
                                @RequestParam(defaultValue = "dark") String theme,
                                HttpSession session,
                                RedirectAttributes ra) {
        Object accountIdObj = session.getAttribute("accountId");
        if (accountIdObj == null) return "redirect:/";

        Integer accountId;
        if (accountIdObj instanceof Integer i) accountId = i;
        else accountId = Integer.valueOf(accountIdObj.toString());

        try {
            employeeService.updateProfile(accountId, name, phone, email, gender);
            session.setAttribute("username", name); // Update session name
            session.setAttribute("userGender", gender); // Update session gender
            ra.addFlashAttribute("success", true);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile?lang=" + lang + "&theme=" + theme;
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file,
                               @RequestParam(defaultValue = "en") String lang,
                               @RequestParam(defaultValue = "dark") String theme,
                               HttpSession session,
                               RedirectAttributes ra) {
        Object accountIdObj = session.getAttribute("accountId");
        if (accountIdObj == null) return "redirect:/";
        Integer accountId = (accountIdObj instanceof Integer i) ? i : Integer.valueOf(accountIdObj.toString());

        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/profile?lang=" + lang + "&theme=" + theme;
        }

        try {
            String filename = accountId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            // Path relative to project root for static serving
            Path uploadPath = Paths.get("src/main/resources/static/uploads/avatars/");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String dbPath = "/uploads/avatars/" + filename;
            employeeService.updateAvatar(accountId, dbPath);
            session.setAttribute("userProfilePic", dbPath); // Store in session for global use
            ra.addFlashAttribute("success", true);
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Could not upload file: " + e.getMessage());
        }

        return "redirect:/profile?lang=" + lang + "&theme=" + theme;
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}

