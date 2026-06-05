package com.saftyhub.project1.repository;

import com.saftyhub.project1.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Integer> {
}
