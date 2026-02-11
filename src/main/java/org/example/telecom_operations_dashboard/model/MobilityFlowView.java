package org.example.telecom_operations_dashboard.model;

import java.math.BigDecimal;

public interface MobilityFlowView {
    Integer getCellId();
    String getProvincia();
    BigDecimal getCellToProvince();
    BigDecimal getProvinceToCell();
}
