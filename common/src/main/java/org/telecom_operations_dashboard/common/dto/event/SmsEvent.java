package org.telecom_operations_dashboard.common.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SmsEvent(
        OffsetDateTime hour,
        Integer        cellId,
        BigDecimal     totalSmsin,
        BigDecimal     totalSmsout
) {}
