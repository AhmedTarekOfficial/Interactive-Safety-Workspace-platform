package com.saftyhub.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saftyhub.project1.model.courses_information;
import java.util.List;

public interface CoursesInformationRepository extends JpaRepository<courses_information, Integer> {
    List<courses_information> findAll();
}

