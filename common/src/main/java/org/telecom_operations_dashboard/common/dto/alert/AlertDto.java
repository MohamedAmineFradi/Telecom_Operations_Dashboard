package org.telecom_operations_dashboard.common.dto.alert;

public record AlertDto(
        Long id,
        Integer cellId,
        String type,
        String severity,
        String message,
        String timestamp
) {}
