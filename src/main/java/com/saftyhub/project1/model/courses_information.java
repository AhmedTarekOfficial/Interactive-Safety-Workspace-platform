package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "courses_information")
public class courses_information {
    
    @Id
    @Column(name = "course_id")
    private Integer courseId;
    
    @Column(name = "course_title", nullable = false, length = 100)
    private String courseTitle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "diffculty_status", columnDefinition = "ENUM('Beginner', 'Intermidiate', 'Advanced')")
    private DifficultyStatus difficultyStatus;
    
    @Column(name = "course_description", nullable = false, length = 100)
    private String courseDescription;
    
    // Explicit setters (in case Lombok isn't processing)
    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
    
    public void setDifficultyStatus(DifficultyStatus difficultyStatus) {
        this.difficultyStatus = difficultyStatus;
    }
    
    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }
    
    public enum DifficultyStatus {
        Beginner,
        Intermidiate,
        Advanced
    }
}
