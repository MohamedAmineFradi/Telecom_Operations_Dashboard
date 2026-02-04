package org.example.telecom_operations_dashboard.service.Impl;

import org.example.telecom_operations_dashboard.controller.exception.ResourceNotFoundException;
import org.example.telecom_operations_dashboard.dto.CellDetailsDto;
import org.example.telecom_operations_dashboard.model.GridCellView;
import org.example.telecom_operations_dashboard.model.TrafficRecord;
import org.example.telecom_operations_dashboard.repository.HourlyTrafficRepository;
import org.example.telecom_operations_dashboard.repository.TrafficRecordRepository;
import org.example.telecom_operations_dashboard.service.CellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CellServiceImpl implements CellService {

    private static final Logger log = LoggerFactory.getLogger(CellServiceImpl.class);

    private final TrafficRecordRepository trafficRecordRepository;
    private final HourlyTrafficRepository hourlyTrafficRepository;

    public CellServiceImpl(TrafficRecordRepository trafficRecordRepository,
                           HourlyTrafficRepository hourlyTrafficRepository) {
        this.trafficRecordRepository = trafficRecordRepository;
        this.hourlyTrafficRepository = hourlyTrafficRepository;
    }

    @Override
    public CellDetailsDto getCellDetails(Integer cellId) {
        TrafficRecord record = trafficRecordRepository.findFirstByCellIdOrderByDatetimeDesc(cellId)
                .orElseThrow(() -> new ResourceNotFoundException("Cell not found: " + cellId));

        BigDecimal smsIn = safeValue(record.getSmsin());
        BigDecimal smsOut = safeValue(record.getSmsout());
        BigDecimal callIn = safeValue(record.getCallin());
        BigDecimal callOut = safeValue(record.getCallout());
        BigDecimal internet = safeValue(record.getInternet());
        BigDecimal total = smsIn.add(smsOut).add(callIn).add(callOut).add(internet);

        GridCellView gridCell = hourlyTrafficRepository.findGridCell(cellId);

        log.info("Cell details requested: cellId={}, lastSeen={}", cellId, record.getDatetime());

        return new CellDetailsDto(
                record.getCellId(),
                record.getCountrycode(),
                record.getDatetime() != null ? record.getDatetime().toString() : null,
                smsIn,
                smsOut,
                callIn,
                callOut,
                internet,
            total,
            gridCell != null ? gridCell.getBounds() : null,
            gridCell != null ? gridCell.getCentroidX() : null,
            gridCell != null ? gridCell.getCentroidY() : null
        );
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
