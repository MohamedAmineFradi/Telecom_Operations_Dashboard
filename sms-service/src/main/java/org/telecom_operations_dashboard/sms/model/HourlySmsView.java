package org.telecom_operations_dashboard.sms.model;

import java.math.BigDecimal;
import java.time.Instant;

public interface HourlySmsView {
    Instant getHour();
    Integer getCellId();
    BigDecimal getTotalSmsin();
    BigDecimal getTotalSmsout();
    BigDecimal getTotalActivity();
}
