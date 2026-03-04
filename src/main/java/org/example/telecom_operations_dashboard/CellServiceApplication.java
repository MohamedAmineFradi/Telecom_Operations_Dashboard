package org.example.telecom_operations_dashboard;

import org.springframework.boot.SpringApplication;

import java.util.Map;

public class CellServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TelecomOperationsDashboardApplication.class);
        app.setDefaultProperties(Map.of("spring.application.name", "CELL-SERVICE"));
        app.run(args);
    }
}
