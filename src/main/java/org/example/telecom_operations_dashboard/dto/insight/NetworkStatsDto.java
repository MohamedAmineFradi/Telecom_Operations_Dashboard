package org.example.telecom_operations_dashboard.dto.insight;

public record NetworkStatsDto(
        long totalAlerts,
        long totalCells,
        long totalTrafficRecords,
        String latestTrafficAt
) {}
