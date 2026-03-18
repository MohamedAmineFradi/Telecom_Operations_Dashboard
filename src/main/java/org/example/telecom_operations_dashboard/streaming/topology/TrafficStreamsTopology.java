package org.example.telecom_operations_dashboard.streaming.topology;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindowedKStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.Stores;
import org.example.telecom_operations_dashboard.streaming.mapper.TrafficEventMapper;
import org.example.telecom_operations_dashboard.streaming.dto.event.CallEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.InternetEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.SmsEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.TrafficEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.util.regex.Pattern;

@Configuration
@ConditionalOnProperty(prefix = "app.streaming", name = "enabled", havingValue = "true")
public class TrafficStreamsTopology {

    private static final Logger log = LoggerFactory.getLogger(TrafficStreamsTopology.class);
    private static final Pattern TRAFFIC_KEY_PATTERN = Pattern.compile("^\\d+:\\d+$");

    @Value("${kafka.topics.sms:activity.sms}")
    private String smsTopic;

    @Value("${kafka.topics.call:activity.call}")
    private String callTopic;

    @Value("${kafka.topics.internet:activity.internet}")
    private String internetTopic;

    @Value("${kafka.topics.traffic-realtime:activity.traffic.realtime}")
    private String realtimeTopic;

    private final TrafficEventMapper trafficEventMapper;

    public TrafficStreamsTopology(TrafficEventMapper trafficEventMapper) {
        this.trafficEventMapper = trafficEventMapper;
    }

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Bean
    public KStream<String, TrafficEvent> trafficPipeline(StreamsBuilder builder) {

        JsonSerde<SmsEvent> smsSerde = jsonSerde(SmsEvent.class);
        JsonSerde<CallEvent> callSerde = jsonSerde(CallEvent.class);
        JsonSerde<InternetEvent> internetSerde = jsonSerde(InternetEvent.class);
        JsonSerde<TrafficEvent> trafficSerde = jsonSerde(TrafficEvent.class);

        KStream<String, SmsEvent> smsStream = builder.stream(smsTopic, Consumed.with(Serdes.String(), smsSerde))
                .filter((k, v) -> isValidTrafficKey(k) && isValidSms(v));
        KStream<String, CallEvent> callStream = builder.stream(callTopic, Consumed.with(Serdes.String(), callSerde))
                .filter((k, v) -> isValidTrafficKey(k) && isValidCall(v));
        KStream<String, InternetEvent> internetStream = builder.stream(internetTopic, Consumed.with(Serdes.String(), internetSerde))
                .filter((k, v) -> isValidTrafficKey(k) && isValidInternet(v));

        KStream<String, TrafficEvent> fromSms = smsStream.mapValues(trafficEventMapper::fromSms);

        KStream<String, TrafficEvent> fromCall = callStream.mapValues(trafficEventMapper::fromCall);

        KStream<String, TrafficEvent> fromInternet = internetStream.mapValues(trafficEventMapper::fromInternet);

        KStream<String, TrafficEvent> merged = fromSms
                .merge(fromCall)
                .merge(fromInternet);

        TimeWindowedKStream<String, TrafficEvent> windowed = merged.groupByKey(
                Grouped.with(Serdes.String(), trafficSerde)
        ).windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofHours(1), Duration.ofMinutes(10)));

        windowed.aggregate(
                        TrafficEvent::new,
                        (key, incoming, agg) -> trafficEventMapper.accumulate(incoming, agg),
                        Materialized.<String, TrafficEvent>as(
                                Stores.inMemoryWindowStore("traffic-hourly-store", Duration.ofDays(1), Duration.ofHours(1), false)
                        ).withKeySerde(Serdes.String()).withValueSerde(trafficSerde)
                )
                .toStream()
                .peek((k, v) -> log.debug("Emitting realtime traffic key={}", k))
                .map((windowedKey, v) -> new org.apache.kafka.streams.KeyValue<>(windowedKey.key(), v))
                .to(realtimeTopic, Produced.with(Serdes.String(), trafficSerde));

        return merged;
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

    private boolean isValidTrafficKey(String key) {
        boolean valid = key != null && TRAFFIC_KEY_PATTERN.matcher(key).matches();
        if (!valid) {
            log.warn("Dropping traffic record with malformed key='{}' (expected cellId:countrycode)", key);
        }
        return valid;
    }

    private boolean isValidSms(SmsEvent value) {
        boolean valid = value != null
                && value.datetime() != null
                && value.cellId() != null
                && value.countrycode() != null
                && value.smsin() != null
                && value.smsout() != null;
        if (!valid) {
            log.warn("Dropping invalid sms event (null required field)");
        }
        return valid;
    }

    private boolean isValidCall(CallEvent value) {
        boolean valid = value != null
                && value.datetime() != null
                && value.cellId() != null
                && value.countrycode() != null
                && value.callin() != null
                && value.callout() != null;
        if (!valid) {
            log.warn("Dropping invalid call event (null required field)");
        }
        return valid;
    }

    private boolean isValidInternet(InternetEvent value) {
        boolean valid = value != null
                && value.datetime() != null
                && value.cellId() != null
                && value.countrycode() != null
                && value.internet() != null;
        if (!valid) {
            log.warn("Dropping invalid internet event (null required field)");
        }
        return valid;
    }
}