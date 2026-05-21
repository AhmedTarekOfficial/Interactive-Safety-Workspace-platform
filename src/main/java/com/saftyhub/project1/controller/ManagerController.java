package com.saftyhub.project1.controller;

import com.saftyhub.project1.model.*;
import com.saftyhub.project1.repository.*;
import com.saftyhub.project1.services.ManagerService;
import com.saftyhub.project1.services.ValidationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired private ManagerService     managerService;
    @Autowired private ValidationService  validationService;
    @Autowired private UserRepository     userRepository;
    @Autowired private AccountRepository  accountRepository;

    private boolean isManager(HttpSession s) {
        if (s.getAttribute("accountId") == null) return false;
        return "Manager".equalsIgnoreCase((String) s.getAttribute("rulePosition"));
    }
    private boolean isAuthenticated(HttpSession s) { return s.getAttribute("accountId") != null; }

    private void addCommon(Model m, HttpSession s, String lang, String theme) {
        m.addAttribute("lang",         lang  != null ? lang  : "en");
        m.addAttribute("theme",        theme != null ? theme : "light");
        m.addAttribute("rulePosition", s.getAttribute("rulePosition"));
        m.addAttribute("activePage",   "manager");
    }

    // ─── GET /manager ──────────────────────────────────────────────────────────
    @GetMapping
    public String showManagerPage(
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            Model model, HttpSession session) {

        if (!isAuthenticated(session)) return "redirect:/";
        if (!isManager(session))       return "redirect:/dashboard?lang=" + lang + "&theme=" + theme;

        // Keep Employees inside the Manager Dashboard (tabbed UI)
        return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme + "&tab=employees";
    }

    // ─── GET /manager/departments/{depId}/jobs (AJAX) ─────────────────────────
    @GetMapping("/departments/{depId}/jobs")
    @ResponseBody
    public ResponseEntity<?> getJobsByDepartment(@PathVariable int depId, HttpSession session) {
        if (!isAuthenticated(session)) return ResponseEntity.status(401).build();
        if (!isManager(session))       return ResponseEntity.status(403).build();

        List<job_information> jobs = managerService.getJobsByDepartmentId(depId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (job_information j : jobs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("jobId", j.getJob_id());
            m.put("title", j.getJob_title());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    // ─── GET /manager/user/{id} (AJAX) ────────────────────────────────────────
    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserById(@PathVariable int userId, HttpSession session) {
        if (!isAuthenticated(session)) return ResponseEntity.status(401).build();
        if (!isManager(session))       return ResponseEntity.status(403).build();
        Optional<Users> user = managerService.getUserById(userId);
        if (user.isEmpty()) return ResponseEntity.notFound().build();

        Users u = user.get();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId",      u.getUserId());
        map.put("username",    u.getUsername());
        map.put("phoneNumber", u.getPhoneNumber());
        map.put("jobId",       u.getJob() != null ? u.getJob().getJob_id() : null);
        map.put("jobTitle",    u.getJob() != null ? u.getJob().getJob_title() : null);
        map.put("depId",       (u.getJob() != null && u.getJob().getDep() != null) ? u.getJob().getDep().getDep_id() : null);
        map.put("depName",     (u.getJob() != null && u.getJob().getDep() != null) ? u.getJob().getDep().getDep_title() : null);
        map.put("ruleId",      u.getRule() != null ? u.getRule().getId() : null);
        map.put("roleName",    u.getRule() != null ? u.getRule().getName() : "Worker");
        map.put("warningCount", u.getWarningCount());
        managerService.getAccountByUserId(u.getUserId())
                .ifPresent(a -> map.put("email", a.getAccountEmail()));
        return ResponseEntity.ok(map);
    }

    // ─── POST /manager/register ────────────────────────────────────────────────
    @PostMapping("/register")
    public String registerEmployee(
            @RequestParam String username,
            @RequestParam String phoneNumber,
            @RequestParam int    jobId,
            @RequestParam(required=false) Integer ruleId,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            Model model, HttpSession session) {

        if (!isAuthenticated(session)) return "redirect:/";
        if (!isManager(session))       return "redirect:/dashboard?lang=" + lang + "&theme=" + theme;

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("errorMessage", "Username already exists.");
        } else if (accountRepository.findByAccountEmail(email).isPresent()) {
            model.addAttribute("errorMessage", "Email already exists.");
        } else if (!validationService.isValidPassword(password)) {
            model.addAttribute("errorMessage", validationService.getPasswordErrorMessage());
        } else {
            boolean ok = managerService.registerEmployee(username, phoneNumber, jobId, ruleId, email, password);
            model.addAttribute(ok ? "successMessage" : "errorMessage",
                               ok ? "Employee registered successfully!" : "Registration failed.");
        }
        model.addAttribute("users",       managerService.getAllUsers());
        model.addAttribute("departments", managerService.getAllDepartments());
        model.addAttribute("jobs",        managerService.getAllJobs());
        model.addAttribute("roles",       managerService.getAllRoles());
        return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme + "&tab=employees";
    }

    // ─── POST /manager/register (AJAX JSON) ──────────────────────────────────
    @PostMapping(value = "/register", headers = "X-Requested-With=fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerEmployeeAjax(
            @RequestParam String username,
            @RequestParam String phoneNumber,
            @RequestParam int jobId,
            @RequestParam(required = false) Integer ruleId,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {

        if (!isAuthenticated(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        if (!isManager(session))       return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Forbidden"));

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Username already exists."));
        }
        if (accountRepository.findByAccountEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Email already exists."));
        }
        if (!validationService.isValidPassword(password)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", validationService.getPasswordErrorMessage()));
        }

        boolean ok = managerService.registerEmployee(username, phoneNumber, jobId, ruleId, email, password);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Registration failed."));
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ─── POST /manager/update ─────────────────────────────────────────────────
    @PostMapping("/update")
    public String updateEmployee(
            @RequestParam int    userId,
            @RequestParam String username,
            @RequestParam String phoneNumber,
            @RequestParam(required=false) Integer jobId,
            @RequestParam(required=false) Integer ruleId,
            @RequestParam String email,
            @RequestParam(required=false) String password,
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            Model model, HttpSession session) {

        if (!isAuthenticated(session)) return "redirect:/";
        if (!isManager(session))       return "redirect:/dashboard?lang=" + lang + "&theme=" + theme;

        Optional<Users> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
            model.addAttribute("errorMessage", "Username already exists.");
        } else {
            Optional<Account_information> existingAcc = accountRepository.findByAccountEmail(email);
            if (existingAcc.isPresent() && existingAcc.get().getAccountId() != userId) {
                model.addAttribute("errorMessage", "Email already exists.");
            } else if (password != null && !password.isEmpty() && !validationService.isValidPassword(password)) {
                model.addAttribute("errorMessage", validationService.getPasswordErrorMessage());
            } else {
                boolean ok = managerService.updateUser(userId, username, phoneNumber, jobId, ruleId, email, password);
                model.addAttribute(ok ? "successMessage" : "errorMessage",
                                   ok ? "Employee updated successfully!" : "Update failed.");
            }
        }
        model.addAttribute("users",       managerService.getAllUsers());
        model.addAttribute("departments", managerService.getAllDepartments());
        model.addAttribute("jobs",        managerService.getAllJobs());
        model.addAttribute("roles",       managerService.getAllRoles());
        return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme + "&tab=employees";
    }

    // ─── POST /manager/update (AJAX JSON) ────────────────────────────────────
    @PostMapping(value = "/update", headers = "X-Requested-With=fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateEmployeeAjax(
            @RequestParam int userId,
            @RequestParam String username,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) Integer jobId,
            @RequestParam(required = false) Integer ruleId,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            HttpSession session) {

        if (!isAuthenticated(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        if (!isManager(session))       return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Forbidden"));

        Optional<Users> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Username already exists."));
        }

        Optional<Account_information> existingAcc = accountRepository.findByAccountEmail(email);
        if (existingAcc.isPresent() && existingAcc.get().getAccountId() != userId) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Email already exists."));
        }

        if (password != null && !password.isEmpty() && !validationService.isValidPassword(password)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", validationService.getPasswordErrorMessage()));
        }

        boolean ok = managerService.updateUser(userId, username, phoneNumber, jobId, ruleId, email, password);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Update failed."));
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ─── POST /manager/delete ─────────────────────────────────────────────────
    @PostMapping("/delete")
    public String deleteUser(
            @RequestParam int userId,
            @RequestParam(defaultValue="en")    String lang,
            @RequestParam(defaultValue="dark") String theme,
            HttpSession session) {

        if (!isAuthenticated(session)) return "redirect:/";
        if (!isManager(session))       return "redirect:/dashboard?lang=" + lang + "&theme=" + theme;
        managerService.deleteUser(userId);
        return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme + "&tab=employees";
    }

    // ─── POST /manager/delete (AJAX JSON) ────────────────────────────────────
    @PostMapping(value = "/delete", headers = "X-Requested-With=fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUserAjax(
            @RequestParam int userId,
            HttpSession session) {

        if (!isAuthenticated(session)) return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        if (!isManager(session))       return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Forbidden"));

        boolean ok = managerService.deleteUser(userId);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Delete failed."));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ─── POST /manager/warn ──────────────────────────────────────────────────
    @PostMapping("/warn")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String,Object> warnEmployee(
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String,Object> body,
            HttpSession session) {

        java.util.Map<String,Object> result = new java.util.HashMap<>();
        if (!isAuthenticated(session) || !isManager(session)) {
            result.put("error", "Unauthorized"); return result;
        }
        int userId = ((Number) body.get("userId")).intValue();
        Optional<Users> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) { result.put("error", "User not found"); return result; }

        Users user = optUser.get();
        int newCount = (user.getWarningCount() == null ? 0 : user.getWarningCount()) + 1;
        user.setWarningCount(newCount);

        if (newCount >= 3) {
            // Auto-terminate
            userRepository.save(user);
            managerService.deleteUser(userId);
            result.put("fired", true);
            result.put("warnings", newCount);
        } else {
            userRepository.save(user);
            result.put("fired", false);
            result.put("warnings", newCount);
        }
        return result;
    }

    // ─── POST /manager/department/update ─────────────────────────────────────
    @PostMapping("/department/update")
    public String updateDepartment(
            @RequestParam int depId,
            @RequestParam String depTitle,
            @RequestParam(required = false) Integer departmentCapacity,
            @RequestParam(defaultValue="en")   String lang,
            @RequestParam(defaultValue="dark") String theme,
            HttpSession session) {

        if (!isAuthenticated(session) || !isManager(session))
            return "redirect:/?lang=" + lang + "&theme=" + theme;
        if (departmentCapacity != null) managerService.updateDepartment(depId, depTitle, departmentCapacity);
        else managerService.renameDepartment(depId, depTitle);
        return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme + "&tab=departments";
    }

    // ─── POST /manager/department/update (AJAX JSON) ─────────────────────────
    @PostMapping(value = "/department/update", headers = "X-Requested-With=fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDepartmentAjax(
            @RequestParam int depId,
            @RequestParam String depTitle,
            @RequestParam(required = false) Integer departmentCapacity,
            HttpSession session) {

        if (!isAuthenticated(session) || !isManager(session)) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Forbidden"));
        }
        boolean ok = (departmentCapacity != null)
                ? managerService.updateDepartment(depId, depTitle, departmentCapacity)
                : managerService.renameDepartment(depId, depTitle);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Update failed."));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ─── POST /manager/department/create (AJAX JSON) ─────────────────────────
    @PostMapping(value = "/department/create", headers = "X-Requested-With=fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createDepartmentAjax(
            @RequestParam String depTitle,
            @RequestParam int departmentCapacity,
            HttpSession session) {

        if (!isAuthenticated(session) || !isManager(session)) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Forbidden"));
        }
        boolean ok = managerService.createDepartment(depTitle, departmentCapacity);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Create failed."));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ─── POST /manager/department/delete ─────────────────────────────────────
    @PostMapping("/department/delete")
    public String deleteDepartment(
            @RequestParam int depId,
            @RequestParam(defaultValue="en")   String lang,
            @RequestParam(defaultValue="dark") String theme,
            HttpSession session) {

        if (!isAuthenticated(session) || !isManager(session))
            return "redirect:/?lang=" + lang + "&theme=" + theme;
        managerService.deleteDepartment(depId);
        return "redirect:/manager/dashboard?lang=" + lang + "&theme=" + theme + "&tab=departments";
    }

    // ─── POST /manager/department/delete (AJAX JSON) ─────────────────────────
    @PostMapping(value = "/department/delete", headers = "X-Requested-With=fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteDepartmentAjax(
            @RequestParam int depId,
            HttpSession session) {

        if (!isAuthenticated(session) || !isManager(session)) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Forbidden"));
        }
        boolean ok = managerService.deleteDepartment(depId);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Delete failed."));
        return ResponseEntity.ok(Map.of("ok", true));
    }

}