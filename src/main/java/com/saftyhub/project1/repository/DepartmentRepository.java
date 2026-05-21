package com.saftyhub.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.saftyhub.project1.model.departments;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<departments, Integer> {
    List<departments> findAll();

    // Since `departments.dep_id` is not auto-generated in the entity,
    // we need a way to compute the next id for insert.
    @Query("SELECT COALESCE(MAX(d.dep_id), 0) FROM departments d")
    Integer findMaxDepId();
}

