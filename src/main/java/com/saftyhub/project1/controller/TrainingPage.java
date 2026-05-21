package com.saftyhub.project1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrainingPage {

    @GetMapping("/training")
    public String showTrainingPage() {
        return "training";
    }

    // Dedicated standalone courses page — accessible from training panel
    // Does NOT require login and does NOT use the manager shell layout
    @GetMapping("/training/courses")
    public String showStandaloneCourses() {
        return "training-courses";
    }

    @GetMapping("/login/trainingcoworker")
    public String showLoginPage() {
        return "redirect:/training/courses";
    }

    @GetMapping("/fire_station")
    public String showFireStationPage() {
        return "fire_station";
    }

    @GetMapping("/safety_alerts")
    public String showSafetyAlertsPage() {
        return "safety_alerts";
    }

    @GetMapping("/first_aid")
    public String showFirstAidPage() {
        return "first_aid";
    }

    @GetMapping("/scenarios")
    public String showScenariosPage() {
        return "scenarios";
    }
}
