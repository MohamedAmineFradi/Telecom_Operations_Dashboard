package org.telecom_operations_dashboard.alert.service;

public interface EmailService {
    void sendHighCongestionAlert(String cellId, String severity, String message);
}
