package com.saftyhub.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saftyhub.project1.model.job_information;
import java.util.List;

public interface JobRepository extends JpaRepository<job_information, Integer> {
    List<job_information> findAll();
    @Query("SELECT j FROM job_information j WHERE j.dep.dep_id = :depId")
    List<job_information> findByDepartmentId(@Param("depId") int depId);

}

