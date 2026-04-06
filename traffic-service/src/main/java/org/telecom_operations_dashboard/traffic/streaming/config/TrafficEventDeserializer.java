package org.telecom_operations_dashboard.traffic.streaming.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;

public class TrafficEventDeserializer implements Deserializer<TrafficEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public TrafficEvent deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(data, TrafficEvent.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize TrafficEvent", ex);
        }
    }
}
