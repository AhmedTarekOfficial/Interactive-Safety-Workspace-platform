package com.saftyhub.project1.dto;

import com.saftyhub.project1.model.Users;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * Converts Users entity → EmployeeDto for the View layer.
 * Since the DB has no game/course progress columns yet,
 * we derive a deterministic "score" from the user's ID so
 * every employee gets a consistent, realistic-looking value.
 */
@Component
public class EmployeeMapper {

    /* ── deterministic pseudo-random helpers ── */
    private int seed(Integer id, int multiplier) {
        return Math.abs((id == null ? 1 : id) * multiplier) % 101;
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String deptFromJob(Users u) {
        if (u.getJob() == null || u.getJob().getDep() == null) return "General";
        return u.getJob().getDep().getDep_title();
    }

    private String deptArFromJob(Users u) {
        if (u.getJob() == null || u.getJob().getDep() == null) return "عام";
        String dep = u.getJob().getDep().getDep_title();
        return switch (dep.toUpperCase()) {
            case "MANUFACTURING" -> "التصنيع";
            case "LOGISTICS"     -> "اللوجستيات";
            case "MAINTENANCE"   -> "الصيانة";
            case "HR"            -> "الموارد البشرية";
            case "OPERATIONS"    -> "العمليات";
            default              -> dep;
        };
    }

    public EmployeeDto.Summary toSummary(Users user) {
        Integer uid    = user.getUserId();
        int course     = seed(uid, 37);
        int game       = seed(uid, 53);
        int safety     = (course + game) / 2;
        int daysIdle   = seed(uid, 13) % 14;          // 0-13 days
        int completed  = seed(uid, 7) % 5;            // 0-4 courses completed
        int total      = 4 + (seed(uid, 3) % 3);      // 4-6 total
        boolean overdue = daysIdle > 7 && course < 50;

        EmployeeDto.StatusEnum status;
        if (course >= 90) status = EmployeeDto.StatusEnum.COMPLETED;
        else if (daysIdle >= 7) status = EmployeeDto.StatusEnum.PROCRASTINATOR;
        else status = EmployeeDto.StatusEnum.ACTIVE;

        return EmployeeDto.Summary.builder()
                .id(uid)
                .name(user.getUsername())
                .nameAr(user.getUsername())
                .avatarInitials(initials(user.getUsername()))
                .department(deptFromJob(user))
                .departmentAr(deptArFromJob(user))
                .jobTitle(user.getJob() != null ? user.getJob().getJob_title() : "N/A")
                .joinDate(user.getJoinDate())
                .role(user.getRule() != null ? user.getRule().getName() : "Worker")
                .avgCourseProgress(course)
                .completedCourses(completed)
                .totalCourses(total)
                .gameProgress(game)
                .safetyScore(safety)
                .daysSinceActive(daysIdle)
                .trainingDeadline(overdue ? LocalDate.now().minusDays(2).toString() : null)
                .overdue(overdue)
                .status(status)
                .gender(user.getGender())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    public EmployeeDto.Detail toDetail(Users user) {
        EmployeeDto.Summary s = toSummary(user);
        return EmployeeDto.Detail.builder()
                .id(s.getId())
                .name(s.getName())
                .nameAr(s.getNameAr())
                .avatarInitials(s.getAvatarInitials())
                .email("—")
                .department(s.getDepartment())
                .departmentAr(s.getDepartmentAr())
                .jobTitle(s.getJobTitle())
                .joinDate(s.getJoinDate())
                .role(s.getRole())
                .avgCourseProgress(s.getAvgCourseProgress())
                .completedCourses(s.getCompletedCourses())
                .totalCourses(s.getTotalCourses())
                .gameProgress(s.getGameProgress())
                .safetyScore(s.getSafetyScore())
                .daysSinceActive(s.getDaysSinceActive())
                .trainingDeadline(s.getTrainingDeadline())
                .overdue(s.isOverdue())
                .status(s.getStatus())
                .gender(s.getGender())
                .profilePicture(s.getProfilePicture())
                .enrollments(Collections.emptyList())
                .build();
    }
}
