package org.example.telecom_operations_dashboard.dto;

public record AlertDto(
        Long id,
        Integer cellId,
        String type,
        String severity,
        String message,
        String timestamp
) {}
