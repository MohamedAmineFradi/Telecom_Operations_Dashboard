package org.telecom_operations_dashboard.common.exception;

import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, String> violations;

    public ValidationException(String message, Map<String, String> violations) {
        super(message);
        this.violations = violations;
    }

    public Map<String, String> getViolations() {
        return violations;
    }
}
