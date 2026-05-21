package com.saftyhub.project1.dto;

import com.saftyhub.project1.model.SafetyEvent;
import lombok.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SafetyEventDto {

    private Long id;
    private String eventType;   // "TRAINING" | "WARNING"
    private String title;
    private String description;
    private String eventDate;
    private String targetDepartment;
    private String warningMessage;
    private Integer severity;

    // Computed helpers for Thymeleaf
    private String severityLabel;
    private String severityColor;
    private String severityBg;
    private int    severityPct;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static SafetyEventDto fromEntity(SafetyEvent e) {
        SafetyEventDto dto = new SafetyEventDto();
        dto.setId(e.getId());
        dto.setEventType(e.getEventType().name());
        dto.setTitle(e.getTitle());
        dto.setDescription(e.getDescription());
        dto.setEventDate(e.getEventDate() != null ? e.getEventDate().format(FMT) : "");
        dto.setTargetDepartment(e.getTargetDepartment());
        dto.setWarningMessage(e.getWarningMessage());
        dto.setSeverity(e.getSeverity() != null ? e.getSeverity() : 0);

        int sev = dto.getSeverity();
        dto.setSeverityLabel(switch (sev) {
            case 1 -> "Low";
            case 2 -> "Medium";
            case 3 -> "High";
            case 4 -> "Critical";
            default -> "";
        });
        dto.setSeverityColor(switch (sev) {
            case 1 -> "#10b981";
            case 2 -> "#f59e0b";
            case 3 -> "#f97316";
            case 4 -> "#ef4444";
            default -> "#94a3b8";
        });
        dto.setSeverityBg(switch (sev) {
            case 1 -> "rgba(16,185,129,.15)";
            case 2 -> "rgba(245,158,11,.15)";
            case 3 -> "rgba(249,115,22,.15)";
            case 4 -> "rgba(239,68,68,.15)";
            default -> "rgba(148,163,184,.15)";
        });
        dto.setSeverityPct(sev * 25);
        return dto;
    }

    public boolean isWarning() { return "WARNING".equals(eventType); }
}
