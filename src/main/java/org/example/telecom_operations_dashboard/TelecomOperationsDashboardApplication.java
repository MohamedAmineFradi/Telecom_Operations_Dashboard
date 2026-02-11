package org.example.telecom_operations_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TelecomOperationsDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelecomOperationsDashboardApplication.class, args);
    }

}
