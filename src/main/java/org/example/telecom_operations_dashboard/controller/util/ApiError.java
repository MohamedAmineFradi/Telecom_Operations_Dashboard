package org.example.telecom_operations_dashboard.controller.util;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details
) {
    public ApiError(int status, String error, String message, String path) {
        this(OffsetDateTime.now().toString(), status, error, message, path, null);
    }

    public ApiError(int status, String error, String message, String path, Map<String, String> details) {
        this(OffsetDateTime.now().toString(), status, error, message, path, details);
    }
}
