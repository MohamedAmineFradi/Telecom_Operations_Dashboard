package org.example.telecom_operations_dashboard.streaming.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MobilityEvent(
        OffsetDateTime datetime,
        Integer        cellId,
        String         provincia,
        BigDecimal     cell2province,
        BigDecimal     province2cell
) {}
