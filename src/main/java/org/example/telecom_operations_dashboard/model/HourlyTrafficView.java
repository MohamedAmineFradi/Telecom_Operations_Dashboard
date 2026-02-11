package org.example.telecom_operations_dashboard.model;

import java.math.BigDecimal;
import java.time.Instant;

public interface HourlyTrafficView {
    Instant getHour();
    Integer getCellId();
    BigDecimal getTotalSmsin();
    BigDecimal getTotalSmsout();
    BigDecimal getTotalCallin();
    BigDecimal getTotalCallout();
    BigDecimal getTotalInternet();
    BigDecimal getTotalActivity();
}
