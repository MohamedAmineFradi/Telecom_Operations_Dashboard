package org.example.telecom_operations_dashboard.streaming.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SmsEvent(
        OffsetDateTime datetime,
        Integer        cellId,
        Integer        countrycode,
        BigDecimal     smsin,
        BigDecimal     smsout
) {}
