package org.example.producer.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component("kafkaReachability")
@ConditionalOnProperty(name = "producer.kafka-health.enabled", havingValue = "true")
public class KafkaReachabilityHealthIndicator implements HealthIndicator {

    private static final int TIMEOUT_SECONDS = 3;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Override
    public Health health() {
        Map<String, Object> config = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(TIMEOUT_SECONDS * 1000),
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, String.valueOf(TIMEOUT_SECONDS * 1000)
        );

        try (AdminClient admin = AdminClient.create(config)) {
            int topics = admin.listTopics().names().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).size();
            return Health.up()
                    .withDetail("bootstrapServers", bootstrapServers)
                    .withDetail("visibleTopics", topics)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("bootstrapServers", bootstrapServers)
                    .build();
        }
    }
}
