package org.telecom_operations_dashboard.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Event emitted by the Clock Service to synchronize all microservices.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationTickEvent {
    private OffsetDateTime timestamp;
    private double speedMultiplier;
}
