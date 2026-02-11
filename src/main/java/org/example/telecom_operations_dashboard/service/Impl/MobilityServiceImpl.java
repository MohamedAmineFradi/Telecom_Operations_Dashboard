package org.example.telecom_operations_dashboard.service.Impl;

import org.example.telecom_operations_dashboard.dto.MobilityCellProvinceFlowDto;
import org.example.telecom_operations_dashboard.dto.MobilityProvinceSummaryDto;
import org.example.telecom_operations_dashboard.model.MobilityFlowView;
import org.example.telecom_operations_dashboard.model.MobilityProvinceSummaryView;
import org.example.telecom_operations_dashboard.repository.MobilityRecordRepository;
import org.example.telecom_operations_dashboard.service.MobilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MobilityServiceImpl implements MobilityService {

    private static final Logger log = LoggerFactory.getLogger(MobilityServiceImpl.class);

    private final MobilityRecordRepository mobilityRecordRepository;

    public MobilityServiceImpl(MobilityRecordRepository mobilityRecordRepository) {
        this.mobilityRecordRepository = mobilityRecordRepository;
    }

    @Override
        public List<MobilityCellProvinceFlowDto> getMobilityFlowsAtHour(
            OffsetDateTime hour,
            Integer cellId,
            String provincia,
            Integer limit
        ) {
        OffsetDateTime bucket = truncateToHour(hour);
        int safeLimit = normalizeLimit(limit);
        String normalizedProvincia = normalizeProvincia(provincia);
        List<MobilityFlowView> rows = mobilityRecordRepository.findFlowsAtHour(
            bucket,
            cellId,
            normalizedProvincia,
            safeLimit
        );
        log.info("Mobility flows fetched: hour={}, count={}", bucket, rows.size());
        return rows.stream()
                .map(r -> new MobilityCellProvinceFlowDto(
                        r.getCellId(),
                        r.getProvincia(),
                        safeValue(r.getCellToProvince()),
                        safeValue(r.getProvinceToCell()),
                        safeValue(r.getCellToProvince()).add(safeValue(r.getProvinceToCell()))
                ))
                .toList();
    }

    @Override
        public List<MobilityProvinceSummaryDto> getProvinceSummariesAtHour(
            OffsetDateTime hour,
            String provincia,
            Integer limit
        ) {
        OffsetDateTime bucket = truncateToHour(hour);
        int safeLimit = normalizeLimit(limit);
        String normalizedProvincia = normalizeProvincia(provincia);
        List<MobilityProvinceSummaryView> rows = mobilityRecordRepository.findProvinceSummariesAtHour(
            bucket,
            normalizedProvincia,
            safeLimit
        );
        log.info("Province mobility summaries fetched: hour={}, count={}", bucket, rows.size());
        return rows.stream()
                .map(r -> new MobilityProvinceSummaryDto(
                        r.getProvincia(),
                        safeValue(r.getTotalCellToProvince()),
                        safeValue(r.getTotalProvinceToCell()),
                        safeValue(r.getTotalCellToProvince()).add(safeValue(r.getTotalProvinceToCell()))
                ))
                .toList();
    }

    private OffsetDateTime truncateToHour(OffsetDateTime datetime) {
        return datetime.withMinute(0).withSecond(0).withNano(0);
    }

    private int normalizeLimit(Integer limit) {
        int safe = limit == null ? 100 : limit;
        if (safe <= 0) {
            throw new IllegalArgumentException("limit must be >= 1");
        }
        return safe;
    }

    private String normalizeProvincia(String provincia) {
        if (provincia == null || provincia.isBlank()) {
            return null;
        }
        return provincia.trim();
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
