package org.telecom_operations_dashboard.internet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableDiscoveryClient
@org.springframework.context.annotation.Import(org.telecom_operations_dashboard.common.config.CorsConfig.class)
public class InternetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InternetServiceApplication.class, args);
    }
}
