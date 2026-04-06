package org.telecom_operations_dashboard.cell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@org.springframework.context.annotation.Import(org.telecom_operations_dashboard.common.config.CorsConfig.class)
public class CellServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CellServiceApplication.class, args);
    }
}
