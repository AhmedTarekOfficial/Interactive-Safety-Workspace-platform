package com.saftyhub.project1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "courses_category")
public class courses_category {
    
    @Id
    @Column(name = "Category_id")
    private Integer categoryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Category_major", columnDefinition = "ENUM('Personal_Safety', 'Environment_Safety', 'Machines_Saftey')")
    private CategoryMajor categoryMajor;
    
    // Explicit setter (in case Lombok isn't processing)
    public void setCategoryMajor(CategoryMajor categoryMajor) {
        this.categoryMajor = categoryMajor;
    }
    
    public enum CategoryMajor {
        Personal_Safety,
        Environment_Safety,
        Machines_Saftey
    }
}
