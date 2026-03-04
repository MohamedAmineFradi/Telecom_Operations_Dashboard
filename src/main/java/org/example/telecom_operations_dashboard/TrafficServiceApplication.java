package org.example.telecom_operations_dashboard;

import org.springframework.boot.SpringApplication;

import java.util.Map;

public class TrafficServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TelecomOperationsDashboardApplication.class);
        app.setDefaultProperties(Map.of("spring.application.name", "TRAFFIC-SERVICE"));
        app.run(args);
    }
}
