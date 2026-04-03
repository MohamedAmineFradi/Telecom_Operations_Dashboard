package org.telecom_operations_dashboard.alert.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.telecom_operations_dashboard.common.dto.cell.CellDetailsDto;

@Component
@ConditionalOnProperty(name = "app.service", havingValue = "alert")
public class RestCellInfoClient {
    private static final Logger log = LoggerFactory.getLogger(RestCellInfoClient.class);
    private final RestClient restClient;

    public RestCellInfoClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.clients.cell-base-url:${CELL_SERVICE_BASE_URL:http://localhost:8082}}") String cellBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(cellBaseUrl)
                .build();
    }

    public CellDetailsDto fetchCellDetails(Integer cellId) {
        try {
            return restClient.get()
                    .uri("/api/cells/" + cellId)
                    .retrieve()
                    .body(CellDetailsDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch cell details for cellId {}", cellId, e);
            return null;
        }
    }
}
