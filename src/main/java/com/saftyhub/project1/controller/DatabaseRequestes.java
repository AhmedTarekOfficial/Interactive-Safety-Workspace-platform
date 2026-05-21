package com.saftyhub.project1.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.saftyhub.project1.services.*;

@RestController
@RequestMapping("/api/user")
public class DatabaseRequestes {

    private UserAutho authService = new UserAutho();
    private Useroperation userOps = new Useroperation();
    public Enhancment enhanceService = new Enhancment();
    public Insert in;

    
    @PostMapping("/modify-password")
    public ResponseEntity<String> modifyPassword(@RequestParam String email,
                                                 @RequestParam String oldPassword,
                                                 @RequestParam String newPassword) {

        
        boolean validOldPassword = authService.Login(email, oldPassword);
        
        if (!validOldPassword) {
            return ResponseEntity.status(401).body("Old password is incorrect.");
        }

       
        enhanceService = new Enhancment(
            "account_info", 
            new String[]{"Account_password"}, 
            new String[]{newPassword}, 
            new String[]{"account_email"}, 
            new String[]{email}
        );

        if (enhanceService != null) {
            return ResponseEntity.ok("Password Updated Successfully!");
        } else {
            return ResponseEntity.status(400).body("Something went wrong.");
        }
    }

   
    @PostMapping("/apply-course")
    public ResponseEntity<String> applyCourse(@RequestParam String courseName) {

        in = new Insert("Courses", new String[]{courseName}, null);

        if (in != null) {
            return ResponseEntity.ok("Course Applied Successfully!");
        } else {
            return ResponseEntity.status(400).body("Cannot apply for the course.");
        }
    }

    // Optional: Keep this if you want an API endpoint for login too
    // (useful if you add a mobile app or separate frontend later)
    // @PostMapping("/login")
    // public ResponseEntity<String> loginAPI(@RequestParam String email,
    //                                       @RequestParam String password) {

    //     boolean valid = authService.Login(email, password);

    //     if (valid) {
    //         return ResponseEntity.ok("Login Successful!");
    //     } else {
    //         return ResponseEntity.status(401).body("Invalid Email or Password");
    //     }
    // }
}
