package org.telecom_operations_dashboard.mobility.dto.insight;

public record NetworkStatsDto(
        long totalAlerts,
        long totalCells,
        long totalTrafficRecords,
        String latestTrafficAt
) {}
