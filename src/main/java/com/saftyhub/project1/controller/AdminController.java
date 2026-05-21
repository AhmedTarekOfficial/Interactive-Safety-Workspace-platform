package com.saftyhub.project1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.saftyhub.project1.services.AdminService;
import com.saftyhub.project1.services.ValidationService;
import com.saftyhub.project1.model.*;
import com.saftyhub.project1.repository.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ValidationService validationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    // ========== MAIN ADMIN PAGE ==========
    @GetMapping
    public String showAdminPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        
        // Load all data
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("managers", adminService.getManagers());
        model.addAttribute("employees", adminService.getEmployees());
        model.addAttribute("rules", adminService.getAllRules());
        model.addAttribute("departments", adminService.getAllDepartments());
        model.addAttribute("jobs", adminService.getAllJobs());
        model.addAttribute("courses", adminService.getAllCourses());
        model.addAttribute("courseCategories", adminService.getAllCourseCategories());
        model.addAttribute("courseModules", adminService.getAllCourseModules());
        model.addAttribute("moduleVideos", adminService.getAllModuleVideos());
        
        return "admin";
    }

    @PostMapping("/user/assignRule")
    public String assignRuleToUser(@RequestParam int userId, 
                                  @RequestParam int ruleId,
                                  @RequestParam(value = "activeTab", required = false) String activeTab,
                                  RedirectAttributes redirectAttributes) {
        // Handle rule removal
        if (ruleId == -1) {
            try {
                Optional<Users> userOpt = userRepository.findById(userId);
                if (userOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                    return redirectToTab(activeTab);
                }
                
                Users user = userOpt.get();
                user.setRule(null);
                userRepository.save(user);
                
                redirectAttributes.addFlashAttribute("successMessage", "Rule removed successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove rule.");
            }
        } else {
            // Normal rule assignment
            boolean success = adminService.assignRuleToUser(userId, ruleId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Rule assigned successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign rule.");
            }
        }
        return redirectToTab(activeTab);
    }
    
    // ========== CREATE MANAGER / EMPLOYEE ==========
    @PostMapping("/manager/create")
    public String createManager(
            @RequestParam("username") String username,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("jobId") int jobId,
            @RequestParam("ruleId") int ruleId,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        // Validation
        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
            return redirectToTab(activeTab);
        }
        if (accountRepository.findByAccountEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists.");
            return redirectToTab(activeTab);
        }
        if (!validationService.isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", validationService.getPasswordErrorMessage());
            return redirectToTab(activeTab);
        }
        boolean created = adminService.createUser(username, phoneNumber, jobId, ruleId, email, password);
        if (created) {
            redirectAttributes.addFlashAttribute("successMessage", "Manager created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create manager. Check job/rule IDs.");
        }
        return redirectToTab(activeTab);
    }

    @PostMapping("/employee/create")
    public String createEmployee(
            @RequestParam("username") String username,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("jobId") int jobId,
            @RequestParam("ruleId") int ruleId,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        // Validation
        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
            return redirectToTab(activeTab);
        }
        if (accountRepository.findByAccountEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists.");
            return redirectToTab(activeTab);
        }
        if (!validationService.isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", validationService.getPasswordErrorMessage());
            return redirectToTab(activeTab);
        }
        boolean created = adminService.createUser(username, phoneNumber, jobId, ruleId, email, password);
        if (created) {
            redirectAttributes.addFlashAttribute("successMessage", "Employee created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create employee. Check job/rule IDs.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== MANAGER MANAGEMENT ==========
    
    @PostMapping("/manager/delete")
    public String deleteManager(@RequestParam("userId") int userId, 
                               @RequestParam(value = "activeTab", required = false) String activeTab,
                               RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteUser(userId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Manager deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete manager.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/manager/update")
    public String updateManager(
            @RequestParam("userId") int userId,
            @RequestParam("username") String username,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("jobId") int jobId,
            @RequestParam("ruleId") int ruleId,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        
        // Validation
        Optional<Users> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent() && existingUser.get().getUserId() != userId) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
            return redirectToTab(activeTab);
        }
        
        Optional<Account_information> existingAccount = accountRepository.findByAccountEmail(email);
        if (existingAccount.isPresent() && existingAccount.get().getAccountId() != userId) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists.");
            return redirectToTab(activeTab);
        }
        
        if (password != null && !password.isEmpty() && !validationService.isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", validationService.getPasswordErrorMessage());
            return redirectToTab(activeTab);
        }
        
        boolean updated = adminService.updateUser(userId, username, phoneNumber, jobId, ruleId, email, password);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Manager updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update manager.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== EMPLOYEE MANAGEMENT ==========
    
    @PostMapping("/employee/delete")
    public String deleteEmployee(@RequestParam("userId") int userId, 
                                @RequestParam(value = "activeTab", required = false) String activeTab,
                                RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteUser(userId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Employee deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete employee.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/employee/update")
    public String updateEmployee(
            @RequestParam("userId") int userId,
            @RequestParam("username") String username,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("jobId") int jobId,
            @RequestParam("ruleId") int ruleId,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        
        // Validation
        Optional<Users> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent() && existingUser.get().getUserId() != userId) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
            return redirectToTab(activeTab);
        }
        
        Optional<Account_information> existingAccount = accountRepository.findByAccountEmail(email);
        if (existingAccount.isPresent() && existingAccount.get().getAccountId() != userId) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists.");
            return redirectToTab(activeTab);
        }
        
        if (password != null && !password.isEmpty() && !validationService.isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", validationService.getPasswordErrorMessage());
            return redirectToTab(activeTab);
        }
        
        boolean updated = adminService.updateUser(userId, username, phoneNumber, jobId, ruleId, email, password);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update employee.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== RULES MANAGEMENT ==========
    
    @PostMapping("/rule/create")
    public String createRule(@RequestParam("position") String positionStr,
                            @RequestParam(value = "activeTab", required = false) String activeTab,
                            RedirectAttributes redirectAttributes, 
                            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        try {
            boolean created = adminService.createRule(positionStr);
            if (created) {
                redirectAttributes.addFlashAttribute("successMessage", "Rule created successfully!");
            }
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rule with this name already exists.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create rule: " + e.getMessage());
            e.printStackTrace();
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/rule/update")
    public String updateRule(
            @RequestParam("ruleId") int ruleId,
            @RequestParam("position") String positionStr,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        try {
            boolean updated = adminService.updateRule(ruleId, positionStr);
            if (updated) {
                redirectAttributes.addFlashAttribute("successMessage", "Rule updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update rule.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update rule: " + e.getMessage());
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/rule/delete")
    public String deleteRule(@RequestParam("ruleId") int ruleId, 
                            @RequestParam(value = "activeTab", required = false) String activeTab,
                            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteRule(ruleId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Rule deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete rule.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== DEPARTMENT MANAGEMENT ==========
    
    @PostMapping("/department/create")
    public String createDepartment(
            @RequestParam("depTitle") String depTitle,
            @RequestParam("departmentCapacity") int departmentCapacity,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean created = adminService.createDepartment(depTitle, departmentCapacity);
        if (created) {
            redirectAttributes.addFlashAttribute("successMessage", "Department created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create department.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/department/update")
    public String updateDepartment(
            @RequestParam("depId") int depId,
            @RequestParam("depTitle") String depTitle,
            @RequestParam("departmentCapacity") int departmentCapacity,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean updated = adminService.updateDepartment(depId, depTitle, departmentCapacity);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Department updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update department.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/department/delete")
    public String deleteDepartment(@RequestParam("depId") int depId, 
                                  @RequestParam(value = "activeTab", required = false) String activeTab,
                                  RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteDepartment(depId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Department deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete department.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== JOB MANAGEMENT ==========
    
    @PostMapping("/job/create")
    public String createJob(
            @RequestParam("jobTitle") String jobTitle,
            @RequestParam("departmentId") int departmentId,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean created = adminService.createJob(jobTitle, departmentId);
        if (created) {
            redirectAttributes.addFlashAttribute("successMessage", "Job created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create job.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/job/update")
    public String updateJob(
            @RequestParam("jobId") int jobId,
            @RequestParam("jobTitle") String jobTitle,
            @RequestParam("departmentId") int departmentId,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean updated = adminService.updateJob(jobId, jobTitle, departmentId);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Job updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update job.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/job/delete")
    public String deleteJob(@RequestParam("jobId") int jobId, 
                           @RequestParam(value = "activeTab", required = false) String activeTab,
                           RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteJob(jobId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Job deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete job.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== COURSE INFORMATION MANAGEMENT ==========
    
    @PostMapping("/course/create")
    public String createCourse(
            @RequestParam("courseTitle") String courseTitle,
            @RequestParam("difficultyStatus") String difficultyStatusStr,
            @RequestParam("courseDescription") String courseDescription,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        try {
            courses_information.DifficultyStatus difficultyStatus = 
                courses_information.DifficultyStatus.valueOf(difficultyStatusStr);
            boolean created = adminService.createCourse(courseTitle, difficultyStatus, courseDescription);
            if (created) {
                redirectAttributes.addFlashAttribute("successMessage", "Course created successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to create course.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid difficulty status.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/course/update")
    public String updateCourse(
            @RequestParam("courseId") int courseId,
            @RequestParam("courseTitle") String courseTitle,
            @RequestParam("difficultyStatus") String difficultyStatusStr,
            @RequestParam("courseDescription") String courseDescription,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        try {
            courses_information.DifficultyStatus difficultyStatus = 
                courses_information.DifficultyStatus.valueOf(difficultyStatusStr);
            boolean updated = adminService.updateCourse(courseId, courseTitle, difficultyStatus, courseDescription);
            if (updated) {
                redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update course.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid difficulty status.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/course/delete")
    public String deleteCourse(@RequestParam("courseId") int courseId, 
                              @RequestParam(value = "activeTab", required = false) String activeTab,
                              RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteCourse(courseId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Course deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete course.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== COURSE CATEGORY MANAGEMENT ==========
    
    @PostMapping("/courseCategory/create")
    public String createCourseCategory(
            @RequestParam("categoryMajor") String categoryMajorStr,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        try {
            courses_category.CategoryMajor categoryMajor = 
                courses_category.CategoryMajor.valueOf(categoryMajorStr);
            boolean created = adminService.createCourseCategory(categoryMajor);
            if (created) {
                redirectAttributes.addFlashAttribute("successMessage", "Course category created successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to create course category.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid category major value.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/courseCategory/update")
    public String updateCourseCategory(
            @RequestParam("categoryId") int categoryId,
            @RequestParam("categoryMajor") String categoryMajorStr,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        try {
            courses_category.CategoryMajor categoryMajor = 
                courses_category.CategoryMajor.valueOf(categoryMajorStr);
            boolean updated = adminService.updateCourseCategory(categoryId, categoryMajor);
            if (updated) {
                redirectAttributes.addFlashAttribute("successMessage", "Course category updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update course category.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid category major value.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/courseCategory/delete")
    public String deleteCourseCategory(@RequestParam("categoryId") int categoryId, 
                                      @RequestParam(value = "activeTab", required = false) String activeTab,
                                      RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteCourseCategory(categoryId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Course category deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete course category.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== COURSE MODULES MANAGEMENT ==========
    
    @PostMapping("/courseModule/create")
    public String createCourseModule(
            @RequestParam("moduleName") String moduleName,
            @RequestParam("courseId") int courseId,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean created = adminService.createCourseModule(moduleName, courseId);
        if (created) {
            redirectAttributes.addFlashAttribute("successMessage", "Course module created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create course module.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/courseModule/update")
    public String updateCourseModule(
            @RequestParam("moduleId") int moduleId,
            @RequestParam("moduleName") String moduleName,
            @RequestParam("courseId") int courseId,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean updated = adminService.updateCourseModule(moduleId, moduleName, courseId);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Course module updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update course module.");
        }
        return redirectToTab(activeTab);
    }
    
    @PostMapping("/courseModule/delete")
    public String deleteCourseModule(@RequestParam("moduleId") int moduleId, 
                                    @RequestParam(value = "activeTab", required = false) String activeTab,
                                    RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteCourseModule(moduleId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Course module deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete course module.");
        }
        return redirectToTab(activeTab);
    }

    // ========== MODULE VIDEO MANAGEMENT ==========
    @PostMapping("/moduleVideo/create")
    public String createModuleVideo(
            @RequestParam("videoPath") String videoPath,
            @RequestParam("moduleId") int moduleId,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean created = adminService.createModuleVideo(videoPath, moduleId);
        if (created) {
            redirectAttributes.addFlashAttribute("successMessage", "Module video added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add module video. Check module ID.");
        }
        return redirectToTab(activeTab);
    }

    @PostMapping("/moduleVideo/update")
    public String updateModuleVideo(
            @RequestParam("videoId") int videoId,
            @RequestParam("videoPath") String videoPath,
            @RequestParam("moduleId") int moduleId,
            @RequestParam(value = "activeTab", required = false) String activeTab,
            RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean updated = adminService.updateModuleVideo(videoId, videoPath, moduleId);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Module video updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update module video.");
        }
        return redirectToTab(activeTab);
    }

    @PostMapping("/moduleVideo/delete")
    public String deleteModuleVideo(@RequestParam("videoId") int videoId, 
                                   @RequestParam(value = "activeTab", required = false) String activeTab,
                                   RedirectAttributes redirectAttributes, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/index";
        }
        boolean deleted = adminService.deleteModuleVideo(videoId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Module video deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete module video.");
        }
        return redirectToTab(activeTab);
    }
    
    // ========== HELPER METHODS ==========
    
    private boolean isAdmin(HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return false;
        }
        
        String rulePositionStr = (String) session.getAttribute("rulePosition");
        if (rulePositionStr == null) {
            return false;
        }
        
        return "Admin".equalsIgnoreCase(rulePositionStr);
    }
    
    // Helper method to redirect to specific tab
    private String redirectToTab(String activeTab) {
        if (activeTab != null && !activeTab.isEmpty()) {
            return "redirect:/admin#" + activeTab;
        }
        return "redirect:/admin";
    }
}
