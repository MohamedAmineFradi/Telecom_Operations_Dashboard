package org.telecom_operations_dashboard.traffic.config;

import org.apache.kafka.common.serialization.Serializer;
import org.telecom_operations_dashboard.common.dto.event.CongestionEvent;
import org.telecom_operations_dashboard.common.util.JsonMapperFactory;

public class CongestionEventSerializer implements Serializer<CongestionEvent> {

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = JsonMapperFactory.createJavaTimeObjectMapper();

    @Override
    public byte[] serialize(String topic, CongestionEvent data) {
        if (data == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize CongestionEvent", ex);
        }
    }
}
