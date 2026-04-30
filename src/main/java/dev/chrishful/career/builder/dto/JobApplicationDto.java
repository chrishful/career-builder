package dev.chrishful.career.builder.dto;

import java.time.LocalDate;

public record JobApplicationDto(
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
) {}