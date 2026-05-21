package com.saftyhub.project1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

public class EmployeeDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer   id;
        private String    name;
        private String    nameAr;
        private String    department;
        private String    departmentAr;
        private String    avatarInitials;
        private String    jobTitle;
        private LocalDate joinDate;
        private String    role;
        private int       avgCourseProgress;
        private int       completedCourses;
        private int       totalCourses;
        private int       gameProgress;
        private int       safetyScore;
        private int       daysSinceActive;
        private String    trainingDeadline;
        private boolean   overdue;
        private StatusEnum status;
        private String    gender;
        private String    profilePicture;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Integer   id;
        private String    name;
        private String    nameAr;
        private String    email;
        private String    phoneNumber;
        private String    department;
        private String    departmentAr;
        private String    avatarInitials;
        private String    jobTitle;
        private LocalDate joinDate;
        private String    role;
        private int       avgCourseProgress;
        private int       completedCourses;
        private int       totalCourses;
        private int       gameProgress;
        private int       safetyScore;
        private int       daysSinceActive;
        private String    trainingDeadline;
        private boolean   overdue;
        private StatusEnum status;
        private String    gender;
        private String    profilePicture;
        private List<EnrollmentDto> enrollments;
        private String    courseTitleAr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long totalEmployees;
        private long managers;
        private long workers;
        private long admins;
        private long activeLearners;
        private long procrastinators;
        private int  overallCompletionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeptStat {
        private String name;
        private String nameAr;
        private int    employeeCount;
        private double avgCourseProgress;
        private double avgGameProgress;
        private double overallScore;
    }

    public enum StatusEnum { ACTIVE, PROCRASTINATOR, COMPLETED }
}
