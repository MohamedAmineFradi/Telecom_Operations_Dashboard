package org.example.telecom_operations_dashboard;

import org.springframework.boot.SpringApplication;

import java.util.Map;

public class MobilityServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TelecomOperationsDashboardApplication.class);
        app.setDefaultProperties(Map.of("spring.application.name", "MOBILITY-SERVICE"));
        app.run(args);
    }
}
