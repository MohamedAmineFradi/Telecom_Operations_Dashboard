package org.example.telecom_operations_dashboard.controller.util;

import org.example.telecom_operations_dashboard.controller.exception.InvalidDateTimeException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

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
}
