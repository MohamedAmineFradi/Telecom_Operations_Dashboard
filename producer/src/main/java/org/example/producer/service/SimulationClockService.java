package org.example.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.config.SimulationProperties;
import org.telecom_operations_dashboard.common.dto.event.SimulationTickEvent;

import java.time.OffsetDateTime;

/**
 * Master Clock Service that synchronizes all microservices in the replay
 * simulation.
 */
@Service
public class SimulationClockService {

    private static final Logger log = LoggerFactory.getLogger(SimulationClockService.class);
    private static final String TOPIC_CLOCK = "simulation.clock";
    private static final String CLOCK_STREAM = "global_clock";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WatermarkStore watermarkStore;
    private final SimulationProperties simulationProperties;

    public SimulationClockService(KafkaTemplate<String, Object> kafkaTemplate,
            WatermarkStore watermarkStore,
            SimulationProperties simulationProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.watermarkStore = watermarkStore;
        this.simulationProperties = simulationProperties;
    }

    /**
     * Emits a clock tick aligned with producer delay properties.
     */
    @Scheduled(initialDelayString = "${producer.poll.initial-delay-ms:60000}", fixedDelayString = "${producer.poll.interval-ms:60000}")
    public void tick() {
        OffsetDateTime currentTime = watermarkStore.getLast(CLOCK_STREAM, simulationProperties.getStart());

        if (currentTime.isAfter(simulationProperties.getEnd())) {
            log.info("Simulation reached end time: {}", simulationProperties.getEnd());
            return;
        }

        SimulationTickEvent tick = new SimulationTickEvent(currentTime, 1.0);
        kafkaTemplate.send(TOPIC_CLOCK, "master", tick);

        log.info("Simulation Tick emitted: {}", currentTime);

        // Advance the clock for next time
        watermarkStore.update(CLOCK_STREAM, currentTime.plus(simulationProperties.getStep()));
    }
}
