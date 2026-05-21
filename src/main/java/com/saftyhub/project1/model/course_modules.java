package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "course_modules")
public class course_modules {
    
    @Id
    @Column(name = "Module_id")
    private Integer moduleId;
    
    @Column(name = "Module_name", length = 10)
    private String moduleName;
    
    @ManyToOne
    @JoinColumn(name = "Course_id")
    private courses_information course;
    
    // Explicit setters (in case Lombok isn't processing)
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    public void setCourse(courses_information course) {
        this.course = course;
    }
}
