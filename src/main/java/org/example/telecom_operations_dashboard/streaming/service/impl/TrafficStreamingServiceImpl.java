package org.example.telecom_operations_dashboard.streaming.service.impl;

import org.apache.kafka.streams.KafkaStreams;
import org.example.telecom_operations_dashboard.dto.streaming.StreamStatusDto;
import org.example.telecom_operations_dashboard.streaming.service.TrafficStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.streaming", name = "enabled", havingValue = "true")
public class TrafficStreamingServiceImpl implements TrafficStreamingService {

    private static final Logger log = LoggerFactory.getLogger(TrafficStreamingServiceImpl.class);

    private final StreamsBuilderFactoryBean streamsFactory;

    public TrafficStreamingServiceImpl(StreamsBuilderFactoryBean streamsFactory) {
        this.streamsFactory = streamsFactory;
    }

    @Override
    public void enableStreaming() {
        try {
            if (!streamsFactory.isRunning()) {
                streamsFactory.start();
                log.info("Kafka Streams started");
            } else {
                log.info("Kafka Streams already running");
            }
        } catch (Exception e) {
            log.error("Failed to start Kafka Streams", e);
            throw new RuntimeException("Could not start Kafka Streams", e);
        }
    }

    @Override
    public void disableStreaming() {
        try {
            if (streamsFactory.isRunning()) {
                streamsFactory.stop();
                log.info("Kafka Streams stopped");
            } else {
                log.info("Kafka Streams already stopped");
            }
        } catch (Exception e) {
            log.error("Failed to stop Kafka Streams", e);
            throw new RuntimeException("Could not stop Kafka Streams", e);
        }
    }

    @Override
    public boolean isStreamingEnabled() {
        return streamsFactory.isRunning();
    }

    @Override
    public StreamStatusDto getStatus() {
        KafkaStreams streams = streamsFactory.getKafkaStreams();
        String state = streams != null ? streams.state().toString() : "NOT_INITIALIZED";
        return new StreamStatusDto(state, streamsFactory.isRunning());
    }
}