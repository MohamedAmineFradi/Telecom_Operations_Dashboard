package org.example.telecom_operations_dashboard.dto;

public record NetworkStatsDto(
        long totalAlerts,
        long totalCells,
        long totalTrafficRecords,
        String latestTrafficAt
) {}
