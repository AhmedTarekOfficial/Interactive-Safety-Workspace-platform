package com.saftyhub.project1.repository;

import com.saftyhub.project1.model.SafetyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> {
    List<SafetyEvent> findByEventTypeOrderByEventDateDesc(SafetyEvent.EventType type);
    List<SafetyEvent> findAllByOrderByEventDateDesc();
    long countByEventType(SafetyEvent.EventType type);
    long countByEventTypeAndSeverity(SafetyEvent.EventType type, Integer severity);
}
