package org.telecom_operations_dashboard.call;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableDiscoveryClient
@org.springframework.context.annotation.Import(org.telecom_operations_dashboard.common.config.CorsConfig.class)
public class CallServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CallServiceApplication.class, args);
    }
}
