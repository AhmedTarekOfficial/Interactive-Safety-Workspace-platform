package com.saftyhub.project1.repository;

import com.saftyhub.project1.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Users, Integer> {

    // Search by username (case-insensitive)
    @Query("SELECT u FROM Users u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Users> searchByName(@Param("query") String query);

    // Find by job ID
    @Query("SELECT u FROM Users u WHERE u.job.job_id = :jobId")
    List<Users> findByJobId(@Param("jobId") int jobId);

    // Find managers
    @Query("SELECT u FROM Users u WHERE u.rule.name = 'Manager'")
    List<Users> findManagers();
}
