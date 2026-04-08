package org.telecom_operations_dashboard.traffic.streaming.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.traffic.congestion")
public class TrafficCongestionProperties {

    private BigDecimal lowMax = new BigDecimal("3000");
    private BigDecimal mediumMax = new BigDecimal("5000");

    public BigDecimal normalizedLowMax() {
        return lowMax == null ? BigDecimal.ZERO : lowMax.max(BigDecimal.ZERO);
    }

    public BigDecimal normalizedMediumMax() {
        BigDecimal low = normalizedLowMax();
        if (mediumMax == null) {
            return low;
        }
        return mediumMax.max(low);
    }
}
