package org.telecom_operations_dashboard.alert.model;

public enum AlertSeverity {
    UNKNOWN(0),
    LOW(1),
    HIGH(2),
    CRITICAL(3);

    private final int rank;

    AlertSeverity(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public boolean isStreamable() {
        return this == HIGH || this == CRITICAL;
    }

    public static AlertSeverity from(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNKNOWN;
        }

        String normalized = raw.trim().toUpperCase();
        return switch (normalized) {
            case "LOW" -> LOW;
            case "HIGH", "WARNING" -> HIGH;
            case "CRITICAL" -> CRITICAL;
            default -> UNKNOWN;
        };
    }
}
