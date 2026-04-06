package org.telecom_operations_dashboard.internet.model;

import java.math.BigDecimal;
import java.time.Instant;

public interface HourlyInternetView {
    Instant getHour();
    Integer getCellId();
    BigDecimal getTotalInternet();
    BigDecimal getTotalActivity();
}
