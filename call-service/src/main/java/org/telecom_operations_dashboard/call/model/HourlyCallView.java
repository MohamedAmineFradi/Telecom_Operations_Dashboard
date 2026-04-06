package org.telecom_operations_dashboard.call.model;

import java.math.BigDecimal;
import java.time.Instant;

public interface HourlyCallView {
    Instant getHour();
    Integer getCellId();
    BigDecimal getTotalCallin();
    BigDecimal getTotalCallout();
    BigDecimal getTotalActivity();
}
