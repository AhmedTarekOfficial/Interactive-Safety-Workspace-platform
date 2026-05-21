package com.saftyhub.project1.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saftyhub.project1.model.Account_information;
import com.saftyhub.project1.model.Users;

public interface UserRepository extends JpaRepository<Users, Integer> {

    // Find user by username
    @Query("SELECT u FROM Users u WHERE u.username = :username")
    Optional<Users> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM Users u WHERE u.rule.id = :roleId")
    List<Users> findByRoleId(@Param("roleId") int roleId);

    List<Users> findByRuleIsNull();

    @Query("""
            SELECT DISTINCT u
            FROM Users u
            LEFT JOIN FETCH u.job j
            LEFT JOIN FETCH j.dep
            LEFT JOIN FETCH u.rule
            """)
    List<Users> findAllWithJobDepAndRule();
}