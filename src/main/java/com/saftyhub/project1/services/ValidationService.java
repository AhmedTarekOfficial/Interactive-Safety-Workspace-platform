package com.saftyhub.project1.services;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class ValidationService {
    
    // Password validation: at least 8 characters, contains capital letter, contains at least one symbol
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$"
    );
    
    /**
     * Validates password strength
     * Requirements:
     * - At least 8 characters
     * - Contains at least one capital letter
     * - Contains at least one symbol
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Gets password validation error message
     */
    public String getPasswordErrorMessage() {
        return "Password must be at least 8 characters, contain at least one capital letter, and at least one symbol";
    }
}

