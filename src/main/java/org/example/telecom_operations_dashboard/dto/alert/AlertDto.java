package org.example.telecom_operations_dashboard.dto.alert;

public record AlertDto(
        Long id,
        Integer cellId,
        String type,
        String severity,
        String message,
        String timestamp
) {}
