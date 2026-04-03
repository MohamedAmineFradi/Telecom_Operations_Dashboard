package org.telecom_operations_dashboard.common.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InternetEvent(
        OffsetDateTime hour,
        Integer        cellId,
        BigDecimal     totalInternet
) {}
