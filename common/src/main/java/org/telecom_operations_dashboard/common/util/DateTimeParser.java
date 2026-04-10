package org.telecom_operations_dashboard.common.util;

import org.telecom_operations_dashboard.common.exception.InvalidDateTimeException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class DateTimeParser {

    private DateTimeParser() {
    }

    public static OffsetDateTime parse(String value, String fieldName) {
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(value).atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ex2) {
                throw new InvalidDateTimeException(fieldName, value);
            }
        }
    }

    public static Optional<OffsetDateTime> parseIfPresent(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(parse(value, fieldName));
    }
}
