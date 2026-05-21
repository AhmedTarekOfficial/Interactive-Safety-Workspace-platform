package com.saftyhub.project1.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentDto {
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String courseIcon;
    private int progress;
    private boolean completed;
    private boolean overdue;
    private LocalDate deadline;
    private LocalDateTime completedAt;
    private LocalDateTime enrolledAt;
}
