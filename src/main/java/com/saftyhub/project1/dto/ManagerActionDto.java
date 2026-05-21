package com.saftyhub.project1.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Inbound request payloads from the Manager UI.
 */
public class ManagerActionDto {

    @Data
    public static class UpdateDeadlineRequest {
        @NotNull(message = "Deadline must not be null")
        @Future(message = "Deadline must be in the future")
        private LocalDate deadline;
    }

    @Data
    public static class FilterRequest {
        private String query;
        private String department;
        private String status;  // ACTIVE | PROCRASTINATOR | COMPLETED | ALL
    }
}
