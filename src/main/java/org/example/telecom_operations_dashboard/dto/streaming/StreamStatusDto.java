package org.example.telecom_operations_dashboard.dto.streaming;

public record StreamStatusDto(
        String  streamsState,
        boolean running
) {}