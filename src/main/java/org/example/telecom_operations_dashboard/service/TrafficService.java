package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.repository.HourlyTrafficRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TrafficService {

    private final HourlyTrafficRepository hourlyTrafficRepository;

    public TrafficService(HourlyTrafficRepository repo) {
        this.hourlyTrafficRepository = repo;
    }

    public List<HourlyTrafficView> getTopCellsAtHour(OffsetDateTime hour, int limit) {
        return hourlyTrafficRepository.findTopCellsAtHour(hour, limit);
    }
}
