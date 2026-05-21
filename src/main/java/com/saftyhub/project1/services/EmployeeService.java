package com.saftyhub.project1.services;

import com.saftyhub.project1.dto.EmployeeDto;
import com.saftyhub.project1.dto.EmployeeMapper;
import com.saftyhub.project1.exception.ResourceNotFoundException;
import com.saftyhub.project1.model.Users;
import com.saftyhub.project1.model.departments;
import com.saftyhub.project1.repository.AccountRepository;
import com.saftyhub.project1.repository.DepartmentRepository;
import com.saftyhub.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final AccountRepository    accountRepository;
    private final EmployeeMapper       mapper;

    public List<EmployeeDto.Summary> getAllSummaries() {
        return userRepository.findAll().stream()
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    public EmployeeDto.Detail getDetail(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        
        String email = accountRepository.findById(id)
                .map(acc -> acc.getAccountEmail())
                .orElse("—");
        
        EmployeeDto.Detail detail = mapper.toDetail(user);
        detail.setEmail(email);
        detail.setPhoneNumber(user.getPhoneNumber()); // Ensure phone is set
        return detail;
    }

    @Transactional
    public void updateProfile(Integer id, String name, String phone, String email, String gender) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setUsername(name);
        user.setPhoneNumber(phone);
        user.setGender(gender);
        userRepository.save(user);

        accountRepository.findById(id).ifPresent(acc -> {
            acc.setAccountEmail(email);
            accountRepository.save(acc);
        });
    }

    @Transactional
    public void updateAvatar(Integer id, String filename) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setProfilePicture(filename);
        userRepository.save(user);
    }

    public List<EmployeeDto.Summary> searchByName(String query) {
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername() != null &&
                        u.getUsername().toLowerCase().contains(query.toLowerCase()))
                .map(mapper::toSummary)
                .collect(Collectors.toList());
    }

    public List<EmployeeDto.Summary> searchAndFilter(String query, String department, String status) {
        return userRepository.findAll().stream()
                .map(mapper::toSummary)
                .filter(e -> query == null || query.isBlank() ||
                             e.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(e -> department == null || department.equals("ALL") ||
                             e.getDepartment().equalsIgnoreCase(department))
                .filter(e -> status == null || status.equals("ALL") ||
                             e.getStatus().name().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public EmployeeDto.Stats getStats() {
        List<EmployeeDto.Summary> all = getAllSummaries();
        long managers = all.stream().filter(e -> "Manager".equalsIgnoreCase(e.getRole())).count();
        long admins   = all.stream().filter(e -> "Admin".equalsIgnoreCase(e.getRole())).count();
        long workers  = all.size() - managers - admins;
        long active   = all.stream().filter(e -> e.getStatus() == EmployeeDto.StatusEnum.ACTIVE).count();
        long procr    = all.stream().filter(e -> e.getStatus() == EmployeeDto.StatusEnum.PROCRASTINATOR).count();
        int  rate     = all.isEmpty() ? 0
                : (int) all.stream().mapToInt(EmployeeDto.Summary::getAvgCourseProgress).average().orElse(0);

        return EmployeeDto.Stats.builder()
                .totalEmployees(all.size())
                .managers(managers)
                .admins(admins)
                .workers(workers)
                .activeLearners(active)
                .procrastinators(procr)
                .overallCompletionRate(rate)
                .build();
    }

    /** Department stats for Manager Dashboard charts */
    public List<EmployeeDto.DeptStat> getDeptStats() {
        List<EmployeeDto.Summary> all = getAllSummaries();

        // Group by department name
        Map<String, List<EmployeeDto.Summary>> byDept = all.stream()
                .collect(Collectors.groupingBy(e ->
                        e.getDepartment() == null || e.getDepartment().isBlank() ? "General" : e.getDepartment()));

        return byDept.entrySet().stream().map(entry -> {
            String dept = entry.getKey();
            List<EmployeeDto.Summary> emps = entry.getValue();
            double avgCourse = emps.stream().mapToInt(EmployeeDto.Summary::getAvgCourseProgress).average().orElse(0);
            double avgGame   = emps.stream().mapToInt(EmployeeDto.Summary::getGameProgress).average().orElse(0);
            double overall   = (avgCourse + avgGame) / 2.0;

            String deptAr = switch (dept.toUpperCase()) {
                case "MANUFACTURING" -> "التصنيع";
                case "LOGISTICS"     -> "اللوجستيات";
                case "MAINTENANCE"   -> "الصيانة";
                case "HR"            -> "الموارد البشرية";
                case "OPERATIONS"    -> "العمليات";
                default              -> dept;
            };

            return EmployeeDto.DeptStat.builder()
                    .name(dept)
                    .nameAr(deptAr)
                    .employeeCount(emps.size())
                    .avgCourseProgress(avgCourse)
                    .avgGameProgress(avgGame)
                    .overallScore(overall)
                    .build();
        })
        .sorted(Comparator.comparingDouble(EmployeeDto.DeptStat::getOverallScore).reversed())
        .collect(Collectors.toList());
    }
}
