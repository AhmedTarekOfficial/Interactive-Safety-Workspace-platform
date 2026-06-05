package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {

    @Id
    @Column(name = "user_id")
    private Integer userId; // Serves as both PK and FK

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "course_progress", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int courseProgress = 0;

    @Column(name = "game_progress", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int gameProgress = 0;

    @Column(name = "safety_score", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int safetyScore = 0;

    @Column(name = "completed_courses", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int completedCourses = 0;

    @Column(name = "total_courses", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int totalCourses = 0;

    @Column(name = "days_since_active", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int daysSinceActive = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;
}
