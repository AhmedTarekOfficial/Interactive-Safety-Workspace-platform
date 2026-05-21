package com.saftyhub.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saftyhub.project1.model.courses_category;
import java.util.List;

public interface CoursesCategoryRepository extends JpaRepository<courses_category, Integer> {
    List<courses_category> findAll();
}

