package org.example.telecom_operations_dashboard.dto;

public record GridCellDto(
        Integer cellId,
        String bounds,
        Double centroidX,
        Double centroidY
) {}
