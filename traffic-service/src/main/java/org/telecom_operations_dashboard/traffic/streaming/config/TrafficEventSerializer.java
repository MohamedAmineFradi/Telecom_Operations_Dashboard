package org.telecom_operations_dashboard.traffic.streaming.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serializer;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;

public class TrafficEventSerializer implements Serializer<TrafficEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public byte[] serialize(String topic, TrafficEvent data) {
        if (data == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize TrafficEvent", ex);
        }
    }
}
