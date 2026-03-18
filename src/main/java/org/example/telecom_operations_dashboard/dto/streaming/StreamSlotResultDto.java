package org.example.telecom_operations_dashboard.dto.streaming;

public record StreamSlotResultDto(
        String slotDatetime,
        int sentEvents
) {}
