package com.saftyhub.project1.repository;

import com.saftyhub.project1.model.courses_information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Enrollment repository stub — kept for compatibility.
 * Core enrollment logic is handled via CoursesInformationRepository.
 */
@Repository
public interface CourseEnrollmentRepository extends JpaRepository<courses_information, Integer> {
}
