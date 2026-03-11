package org.example.telecom_operations_dashboard.dto;

import java.util.List;

public record KafkaClientConfigDto(
        List<String> bootstrapServers,
        String topic,
        String keyFormat,
        String valueFormat,
        String datetimeFormat,
        int expectedEventsPerSlot,
        int slotIntervalMs
) {}
