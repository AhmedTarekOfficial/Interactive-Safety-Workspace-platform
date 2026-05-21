package com.saftyhub.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saftyhub.project1.model.course_modules;
import java.util.List;

public interface CourseModulesRepository extends JpaRepository<course_modules, Integer> {
    List<course_modules> findAll();
}

