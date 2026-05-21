package com.saftyhub.project1.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.saftyhub.project1.model.Rules;

public interface RulesRepository extends JpaRepository<Rules, Integer> {

    // Find role by name e.g. "Manager", "Admin", "Worker"
    Optional<Rules> findByName(String name);

    @Query("SELECT MAX(r.id) FROM Rules r")
    Integer findMaxId();
}
