package org.telecom_operations_dashboard.mobility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableDiscoveryClient
@SpringBootApplication
@org.springframework.context.annotation.Import(org.telecom_operations_dashboard.common.config.CorsConfig.class)
public class MobilityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MobilityServiceApplication.class, args);
    }
}
