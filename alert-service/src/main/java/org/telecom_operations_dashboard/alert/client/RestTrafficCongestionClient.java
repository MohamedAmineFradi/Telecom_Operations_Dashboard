package org.telecom_operations_dashboard.alert.client;

import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.service", havingValue = "alert")
public class RestTrafficCongestionClient implements TrafficCongestionClient {

    private static final Logger log = LoggerFactory.getLogger(RestTrafficCongestionClient.class);

    private final RestClient restClient;

    public RestTrafficCongestionClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.clients.traffic-base-url:${TRAFFIC_SERVICE_BASE_URL:http://localhost:8080}}") String trafficBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(trafficBaseUrl)
                .build();
    }

    @Override
    public List<CongestionCellDto> fetchCongestion(
            OffsetDateTime hour,
            int limit,
            double warningThreshold,
            double criticalThreshold
    ) {
        try {
            CongestionCellDto[] response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/traffic/congestion")
                            .queryParam("hour", hour.toString())
                            .queryParam("limit", limit)
                            .queryParam("warn", warningThreshold)
                            .queryParam("crit", criticalThreshold)
                            .build())
                    .retrieve()
                    .body(CongestionCellDto[].class);

            return response == null ? List.of() : Arrays.asList(response);
        } catch (Exception e) {
            log.error("Failed to fetch congestion from traffic-service", e);
            return List.of();
        }
    }
}
