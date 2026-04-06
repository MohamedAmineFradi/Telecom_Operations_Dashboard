package org.telecom_operations_dashboard.common.dto.cell;

public record GridCellDto(
        Integer cellId,
        String bounds,
        Double centroidX,
        Double centroidY
) {}
