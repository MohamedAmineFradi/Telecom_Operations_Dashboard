package org.example.telecom_operations_dashboard.streaming.topology;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.example.telecom_operations_dashboard.streaming.dto.event.MobilityEvent;
import org.example.telecom_operations_dashboard.streaming.mapper.MobilityEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.regex.Pattern;

@Configuration
@ConditionalOnProperty(prefix = "app.streaming", name = "enabled", havingValue = "true")
public class MobilityStreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(MobilityStreamsTopology.class);
    private static final Pattern MOBILITY_KEY_PATTERN = Pattern.compile("^\\d+:.+$");

    @Value("${kafka.topics.mobility:activity.mobility}")
    private String mobilityTopic;

    @Value("${kafka.topics.mobility-realtime:activity.mobility.realtime}")
    private String mobilityRealtimeTopic;

    private final MobilityEventMapper mobilityEventMapper;

    public MobilityStreamsTopology(MobilityEventMapper mobilityEventMapper) {
        this.mobilityEventMapper = mobilityEventMapper;
    }

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Bean
    public KStream<String, MobilityEvent> mobilityPipeline(StreamsBuilder builder) {

        JsonSerde<MobilityEvent> mobilitySerde = jsonSerde(MobilityEvent.class);

        KStream<String, MobilityEvent> stream = builder.stream(
                mobilityTopic, Consumed.with(Serdes.String(), mobilitySerde)
        ).filter((key, value) -> isValidMobilityKey(key) && isValidMobility(value));

        KStream<String, MobilityEvent> filtered = stream.filter((key, value) -> mobilityEventMapper.hasPositiveFlow(value));

        filtered
                .peek((key, value) -> log.debug("Mobility event cell={} province={}", value.cellId(), value.provincia()))
                .to(mobilityRealtimeTopic, Produced.with(Serdes.String(), mobilitySerde));

        return filtered;
    }

    private boolean isValidMobilityKey(String key) {
        boolean valid = key != null && MOBILITY_KEY_PATTERN.matcher(key).matches();
        if (!valid) {
            log.warn("Dropping mobility record with malformed key='{}' (expected cellId:provincia)", key);
        }
        return valid;
    }

    private boolean isValidMobility(MobilityEvent value) {
        boolean valid = mobilityEventMapper.hasRequiredFields(value);
        if (!valid) {
            log.warn("Dropping invalid mobility event (null/blank required field)");
        }
        return valid;
    }

    private <T> JsonSerde<T> jsonSerde(Class<T> type) {
        JsonSerde<T> serde = new JsonSerde<>(type, mapper);
        serde.deserializer().setUseTypeHeaders(false);
        serde.deserializer().addTrustedPackages(
                "org.example.telecom_operations_dashboard.dto",
                "org.example.telecom_operations_dashboard.streaming.dto",
                "org.example.producer.dto"
        );
        return serde;
    }
}