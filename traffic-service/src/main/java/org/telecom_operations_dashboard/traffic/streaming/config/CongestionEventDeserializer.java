package org.telecom_operations_dashboard.traffic.streaming.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.telecom_operations_dashboard.common.dto.event.CongestionEvent;

public class CongestionEventDeserializer implements Deserializer<CongestionEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public CongestionEvent deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(data, CongestionEvent.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize CongestionEvent", ex);
        }
    }
}
