package org.example.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.SimulationTickEvent;

import java.util.List;

/**
 * Listens for synchronization ticks and triggers producers.
 */
@Service
public class ProducerTickListener {

    private static final Logger log = LoggerFactory.getLogger(ProducerTickListener.class);

    private final List<AbstractReplayProducer<?>> producers;

    public ProducerTickListener(List<AbstractReplayProducer<?>> producers) {
        this.producers = producers != null ? producers : java.util.List.of();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("ProducerTickListener initialized with {} producers", producers.size());
    }

    @KafkaListener(topics = "simulation.clock", groupId = "producer-clock-group")
    public void onTick(SimulationTickEvent event) {
        log.debug("Received simulation tick: {}", event.getTimestamp());
        for (AbstractReplayProducer<?> producer : producers) {
            try {
                producer.doPoll(event.getTimestamp());
            } catch (Exception e) {
                log.error("Failed to process tick for producer: {}", producer.getClass().getSimpleName(), e);
            }
        }
    }
}
