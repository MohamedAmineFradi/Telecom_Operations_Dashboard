package org.example.telecom_operations_dashboard.streaming.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "app.streaming", name = "enabled", havingValue = "true")
public class KafkaTopicsConfig {

    @Bean
    public NewTopic smsTopic(@Value("${kafka.topics.sms:activity.sms}") String topicName) {
        return buildTopic(topicName);
    }

    @Bean
    public NewTopic callTopic(@Value("${kafka.topics.call:activity.call}") String topicName) {
        return buildTopic(topicName);
    }

    @Bean
    public NewTopic internetTopic(@Value("${kafka.topics.internet:activity.internet}") String topicName) {
        return buildTopic(topicName);
    }

    @Bean
    public NewTopic mobilityTopic(@Value("${kafka.topics.mobility:activity.mobility}") String topicName) {
        return buildTopic(topicName);
    }

    @Bean
    public NewTopic trafficRealtimeTopic(@Value("${kafka.topics.traffic-realtime:activity.traffic.realtime}") String topicName) {
        return buildTopic(topicName);
    }

    @Bean
    public NewTopic mobilityRealtimeTopic(@Value("${kafka.topics.mobility-realtime:activity.mobility.realtime}") String topicName) {
        return buildTopic(topicName);
    }

    private NewTopic buildTopic(String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(2)
                .replicas(1)
                .build();
    }
}