package dev.chrishful.career.builder.dto;

import java.time.LocalDate;

public record JobApplicationDto(
        int number,
        String company,
        String role,
        LocalDate dateApplied,
        String status,
        String interested,
        String salaryEstimate,
        boolean remote,
        LocalDate lastUpdated,
        String notes
) {}