package org.telecom_operations_dashboard.traffic.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.telecom_operations_dashboard.common.util.JsonMapperFactory;

public class TrafficEventDeserializer implements Deserializer<TrafficEvent> {

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = JsonMapperFactory.createJavaTimeObjectMapper();

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
