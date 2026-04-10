package org.telecom_operations_dashboard.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Global simulation properties shared across all microservices.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.simulation")
public class SimulationProperties {

    /**
     * The start date and time for the simulation replay.
     */
    private OffsetDateTime start = OffsetDateTime.parse("2013-11-01T00:00:00Z");

    /**
     * The end date and time for the simulation replay.
     */
    private OffsetDateTime end = OffsetDateTime.parse("2013-11-07T23:59:59Z");

    /**
     * The default time step for the simulation replay.
     */
    private Duration step = Duration.ofHours(1);

    /**
     * The number of records to process in a single batch.
     */
    private int batchSize = 100;

    /**
     * Max time to wait for one page of Kafka sends to complete.
     */
    private int pageSendTimeoutSeconds = 15;
}
