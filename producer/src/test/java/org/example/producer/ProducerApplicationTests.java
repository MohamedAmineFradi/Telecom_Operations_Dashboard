package org.example.producer;

import com.netflix.discovery.EurekaClient;
import org.example.producer.repository.HourlyTrafficRecordRepository;
import org.example.producer.repository.MobilityRecordRepository;
import org.example.producer.service.WatermarkStore;
import org.junit.jupiter.api.Test;
import org.telecom_operations_dashboard.common.config.SimulationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

class ProducerApplicationTests extends AbstractIntegrationTest {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private WatermarkStore watermarkStore;

    @MockBean
    private EurekaClient eurekaClient;

    @MockBean
    private SimulationProperties simulationProperties;

    @MockBean
    private MobilityRecordRepository mobilityRecordRepository;

    @MockBean
    private HourlyTrafficRecordRepository hourlyTrafficRecordRepository;

    @Test
    void contextLoads() {
    }

}
