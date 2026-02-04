package org.example.telecom_operations_dashboard.model;

import java.math.BigDecimal;

public interface HeatmapCellView {
    Integer getCellId();
    BigDecimal getTotalActivity();
    String getBounds();
    Double getLon();
    Double getLat();
}
