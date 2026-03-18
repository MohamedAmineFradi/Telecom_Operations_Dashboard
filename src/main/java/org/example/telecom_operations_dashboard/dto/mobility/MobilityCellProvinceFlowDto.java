package org.example.telecom_operations_dashboard.dto.mobility;

import java.math.BigDecimal;

public record MobilityCellProvinceFlowDto(
        Integer cellId,
        String provincia,
        BigDecimal cellToProvince,
        BigDecimal provinceToCell,
        BigDecimal totalFlow
) {}
