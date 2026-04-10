package org.telecom_operations_dashboard.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonMapperFactory {

    private JsonMapperFactory() {
    }

    public static ObjectMapper createJavaTimeObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}