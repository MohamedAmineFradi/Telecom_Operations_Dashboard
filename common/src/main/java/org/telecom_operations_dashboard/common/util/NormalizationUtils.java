package org.telecom_operations_dashboard.common.util;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class NormalizationUtils {

    private NormalizationUtils() {
    }

    public static OffsetDateTime truncateToHour(OffsetDateTime datetime) {
        return datetime.withMinute(0).withSecond(0).withNano(0);
    }

    public static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static int normalizeLimitClamped(Integer limit, int defaultValue, int maxValue) {
        int resolved = limit == null ? defaultValue : limit;
        if (resolved <= 0) {
            return 1;
        }
        return Math.min(resolved, maxValue);
    }

    public static String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
