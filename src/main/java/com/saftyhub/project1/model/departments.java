package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "departments")
public class departments {
    
    @Id
    private int dep_id; 

    @Column(name = "dep_title", nullable = false) 
    private String dep_title;

    @Column(name = "department_capacity", nullable = false)
    private int department_capacity;

    // Explicit getters for fields with underscores
    public int getDep_id() {
        return dep_id;
    }
    
    public String getDep_title() {
        return dep_title;
    }
    
    public int getDepartment_capacity() {
        return department_capacity;
    }

    // Explicit setters for fields with underscores
    public void setDep_id(int dep_id) {
        this.dep_id = dep_id;
    }
    
    public void setDep_title(String dep_title) {
        this.dep_title = dep_title;
    }
    
    public void setDepartment_capacity(int department_capacity) {
        this.department_capacity = department_capacity;
    }
}