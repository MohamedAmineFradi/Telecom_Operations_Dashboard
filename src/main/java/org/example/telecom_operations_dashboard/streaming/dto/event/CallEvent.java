package org.example.telecom_operations_dashboard.streaming.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CallEvent(
        OffsetDateTime datetime,
        Integer        cellId,
        Integer        countrycode,
        BigDecimal     callin,
        BigDecimal     callout
) {}
