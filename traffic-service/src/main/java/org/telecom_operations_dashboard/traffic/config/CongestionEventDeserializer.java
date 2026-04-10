package org.telecom_operations_dashboard.traffic.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.telecom_operations_dashboard.common.dto.event.CongestionEvent;
import org.telecom_operations_dashboard.common.util.JsonMapperFactory;

public class CongestionEventDeserializer implements Deserializer<CongestionEvent> {

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = JsonMapperFactory.createJavaTimeObjectMapper();

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
