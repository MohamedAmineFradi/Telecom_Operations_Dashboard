package org.telecom_operations_dashboard.traffic.config;

import org.apache.kafka.common.serialization.Serializer;
import org.telecom_operations_dashboard.common.util.JsonMapperFactory;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;

public class TrafficEventSerializer implements Serializer<TrafficEvent> {

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = JsonMapperFactory.createJavaTimeObjectMapper();

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
