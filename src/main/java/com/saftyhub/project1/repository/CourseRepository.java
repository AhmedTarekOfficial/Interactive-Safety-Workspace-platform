package com.saftyhub.project1.repository;

import com.saftyhub.project1.model.courses_information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for courses_information — alias kept for backward compatibility.
 * Use CoursesInformationRepository for new code.
 */
@Repository
public interface CourseRepository extends JpaRepository<courses_information, Integer> {
    List<courses_information> findAll();
}
