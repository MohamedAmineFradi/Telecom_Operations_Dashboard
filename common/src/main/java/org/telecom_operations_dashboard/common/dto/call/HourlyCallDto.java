package org.telecom_operations_dashboard.common.dto.call;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record HourlyCallDto(
    Integer cellId,
    OffsetDateTime hour,
    BigDecimal callin,
    BigDecimal callout
) {}
