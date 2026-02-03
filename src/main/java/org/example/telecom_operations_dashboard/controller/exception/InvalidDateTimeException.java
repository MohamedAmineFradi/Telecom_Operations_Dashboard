package org.example.telecom_operations_dashboard.controller.exception;

public class InvalidDateTimeException extends IllegalArgumentException {
    public InvalidDateTimeException(String fieldName, String value) {
        super(String.format("Invalid datetime format for '%s': '%s'. Expected ISO-8601, e.g. 2013-11-01T18:00:00Z", fieldName, value));
    }

    public InvalidDateTimeException(String message) {
        super(message);
    }
}
