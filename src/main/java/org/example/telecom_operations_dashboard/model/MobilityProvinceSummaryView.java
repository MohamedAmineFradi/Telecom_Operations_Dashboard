package org.example.telecom_operations_dashboard.model;

import java.math.BigDecimal;

public interface MobilityProvinceSummaryView {
    String getProvincia();
    BigDecimal getTotalCellToProvince();
    BigDecimal getTotalProvinceToCell();
}
