package com.saftyhub.project1.services;

import com.saftyhub.project1.dto.SafetyEventDto;
import com.saftyhub.project1.exception.ResourceNotFoundException;
import com.saftyhub.project1.model.SafetyEvent;
import com.saftyhub.project1.repository.SafetyEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafetyEventService {

    private final SafetyEventRepository repo;

    public List<SafetyEventDto> getAll() {
        return repo.findAllByOrderByEventDateDesc().stream()
                .map(SafetyEventDto::fromEntity).collect(Collectors.toList());
    }

    public List<SafetyEventDto> getByType(SafetyEvent.EventType type) {
        return repo.findByEventTypeOrderByEventDateDesc(type).stream()
                .map(SafetyEventDto::fromEntity).collect(Collectors.toList());
    }

    public long countTraining()  { return repo.countByEventType(SafetyEvent.EventType.TRAINING); }
    public long countWarnings()  { return repo.countByEventType(SafetyEvent.EventType.WARNING); }
    public long countCritical()  { return repo.countByEventTypeAndSeverity(SafetyEvent.EventType.WARNING, 4); }
    public long countAll()       { return repo.count(); }

    @Transactional
    public void create(String type, String title, String description,
                       String date, String dept, String warnMsg, Integer severity) {
        SafetyEvent.EventType evType = SafetyEvent.EventType.valueOf(type.toUpperCase());
        SafetyEvent ev = SafetyEvent.builder()
                .eventType(evType)
                .title(title)
                .description(description)
                .eventDate(date != null && !date.isBlank() ? LocalDate.parse(date) : LocalDate.now())
                .targetDepartment(dept)
                .warningMessage(evType == SafetyEvent.EventType.WARNING ? warnMsg : null)
                .severity(evType == SafetyEvent.EventType.WARNING ? severity : null)
                .build();
        repo.save(ev);
    }

    @Transactional
    public void update(Long id, String type, String title, String description,
                       String date, String dept, String warnMsg, Integer severity) {
        SafetyEvent ev = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        SafetyEvent.EventType evType = SafetyEvent.EventType.valueOf(type.toUpperCase());
        ev.setEventType(evType);
        ev.setTitle(title);
        ev.setDescription(description);
        if (date != null && !date.isBlank()) ev.setEventDate(LocalDate.parse(date));
        ev.setTargetDepartment(dept);
        ev.setWarningMessage(evType == SafetyEvent.EventType.WARNING ? warnMsg : null);
        ev.setSeverity(evType == SafetyEvent.EventType.WARNING ? severity : null);
        repo.save(ev);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Event not found: " + id);
        repo.deleteById(id);
    }
}
