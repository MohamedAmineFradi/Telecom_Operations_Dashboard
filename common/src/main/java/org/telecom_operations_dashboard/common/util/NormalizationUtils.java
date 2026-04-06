package org.telecom_operations_dashboard.common.util;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public final class NormalizationUtils {

    private static final Set<String> ALLOWED_STEPS = Set.of("minute", "hour", "day");

    private NormalizationUtils() {
    }

    public static OffsetDateTime truncateToHour(OffsetDateTime datetime) {
        return datetime.withMinute(0).withSecond(0).withNano(0);
    }

    public static OffsetDateTime truncateByStep(OffsetDateTime datetime, String step) {
        return switch (step) {
            case "minute" -> datetime.withSecond(0).withNano(0);
            case "day" -> datetime.toLocalDate().atStartOfDay().atOffset(datetime.getOffset());
            case "hour" -> truncateToHour(datetime);
            default -> throw new IllegalArgumentException("Unsupported step: " + step);
        };
    }

    public static void validateThresholdsOrThrow(double warningThreshold, double criticalThreshold) {
        if (warningThreshold < 0 || warningThreshold > 100 || criticalThreshold < 0 || criticalThreshold > 100) {
            throw new IllegalArgumentException("thresholds must be between 0 and 100");
        }
        if (warningThreshold >= criticalThreshold) {
            throw new IllegalArgumentException("warningThreshold must be < criticalThreshold");
        }
    }

    public static int normalizeLimitOrThrow(Integer limit, int defaultValue) {
        int resolved = limit == null ? defaultValue : limit;
        if (resolved <= 0) {
            throw new IllegalArgumentException("limit must be >= 1");
        }
        return resolved;
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

    public static String normalizeStepOrThrow(String step, String defaultValue) {
        String resolved = step == null || step.isBlank() ? defaultValue : step.trim().toLowerCase();
        if (!ALLOWED_STEPS.contains(resolved)) {
            throw new IllegalArgumentException("Unsupported step: " + resolved);
        }
        return resolved;
    }

    public static void validateDateRangeOrThrow(OffsetDateTime from, OffsetDateTime to, long maxDays) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be <= to");
        }

        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween > maxDays) {
            throw new IllegalArgumentException("Maximum date range is " + maxDays + " days; requested " + daysBetween + " days");
        }
    }
}
