package org.example.telecom_operations_dashboard.dto;

public record StreamStatusDto(
        String currentSlot,
        String lastSentAt,
        int eventsPerSlot
) {}