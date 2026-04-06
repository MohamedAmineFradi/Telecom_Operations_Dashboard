package org.telecom_operations_dashboard.alert.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.alert.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    @Value("${app.operator.email:operator@telecom.com}")
    private String operatorEmail;

    @Override
    public void sendHighCongestionAlert(String cellId, String severity, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(operatorEmail);
            mailMessage.setSubject("CRITICAL: High Congestion Alert - Cell " + cellId);
            mailMessage.setText(String.format(
                "High congestion detected!\n\nCell ID: %s\nSeverity: %s\nDetails: %s\n\nPlease take immediate action.",
                cellId, severity, message
            ));

            mailSender.send(mailMessage);
            log.info("Sent high congestion alert email for cell {}", cellId);
        } catch (Exception e) {
            log.error("Failed to send congestion alert email for cell {}: {}", cellId, e.getMessage(), e);
        }
    }
}
