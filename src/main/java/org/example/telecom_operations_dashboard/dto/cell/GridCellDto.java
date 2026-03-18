package org.example.telecom_operations_dashboard.dto.cell;

public record GridCellDto(
        Integer cellId,
        String bounds,
        Double centroidX,
        Double centroidY
) {}
