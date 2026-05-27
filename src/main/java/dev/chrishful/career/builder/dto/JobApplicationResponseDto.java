package dev.chrishful.career.builder.dto;

public record JobApplicationResponseDto(
        boolean success,
        String message,
        Integer number,
        String lastUpdated
) {}