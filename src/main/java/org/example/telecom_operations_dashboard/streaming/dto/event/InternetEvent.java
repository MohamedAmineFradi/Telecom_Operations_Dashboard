package org.example.telecom_operations_dashboard.streaming.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InternetEvent(
        OffsetDateTime datetime,
        Integer        cellId,
        Integer        countrycode,
        BigDecimal     internet
) {}
