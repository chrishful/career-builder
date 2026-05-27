package dev.chrishful.career.builder.dto;

import java.util.UUID;

public record JobApplicationDto(
        UUID id,
        int number,
        String company,
        String role,
        String dateApplied,
        String status,
        String interested,
        String salaryEstimate,
        boolean remote,
        String lastUpdated,
        String notes
) {
    // Companion constructor for incoming API requests (Insert/Creation payloads)
    public JobApplicationDto(String company, String role, String status, String interested, String salaryEstimate, boolean remote, String notes) {
        this(null, 0, company, role, null, status, interested, salaryEstimate, remote, null, notes);
    }
}