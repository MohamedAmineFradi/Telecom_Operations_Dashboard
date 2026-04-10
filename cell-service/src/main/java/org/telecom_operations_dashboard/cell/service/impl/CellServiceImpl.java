package org.telecom_operations_dashboard.cell.service.impl;

import org.telecom_operations_dashboard.common.exception.ResourceNotFoundException;
import org.telecom_operations_dashboard.common.dto.cell.CellDetailsDto;
import org.telecom_operations_dashboard.common.dto.cell.GridCellDto;
import org.telecom_operations_dashboard.cell.model.GridCell;
import org.telecom_operations_dashboard.cell.repository.GridCellRepository;
import org.telecom_operations_dashboard.cell.service.CellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.service", havingValue = "cell")
public class CellServiceImpl implements CellService {

    private static final Logger log = LoggerFactory.getLogger(CellServiceImpl.class);

    private final GridCellRepository gridCellRepository;

    public CellServiceImpl(GridCellRepository gridCellRepository) {
        this.gridCellRepository = gridCellRepository;
    }

    @Override
    public CellDetailsDto getCellDetails(Integer cellId) {
        GridCell cell = gridCellRepository.findById(cellId)
            .orElseThrow(() -> new ResourceNotFoundException("Cell not found: " + cellId));

        log.info("Cell details requested: cellId={}", cellId);

        Double[] centroid = computeCentroidFromBounds(cell.getBounds());

        return new CellDetailsDto(
            cell.getCellId(),
            Integer.valueOf(39), // countryCode for Italy
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            cell.getBounds(),
            centroid[0],
            centroid[1]
        );
        }

    @Override
    public List<GridCellDto> getAllGridCells() {
        List<GridCell> cells = gridCellRepository.findAllOrderByCellId();
        log.info("Fetched {} grid cells", cells.size());
        return cells.stream()
                .map(c -> {
                    Double[] centroid = computeCentroidFromBounds(c.getBounds());
                    return new GridCellDto(
                        c.getCellId(),
                        c.getBounds(),
                        centroid[0],
                        centroid[1]
                    );
                })
                .toList();
    }

    @Override
    public Page<GridCellDto> getAllGridCells(Pageable pageable) {
        Page<GridCell> cells = gridCellRepository.findAllOrderByCellId(pageable);
        log.info("Fetched page of grid cells with size {}", pageable.getPageSize());
        return cells.map(c -> {
            Double[] centroid = computeCentroidFromBounds(c.getBounds());
            return new GridCellDto(
                c.getCellId(),
                c.getBounds(),
                centroid[0],
                centroid[1]
            );
        });
    }

    /**
     * Computes centroidX and centroidY from a bounds string ("minX,minY,maxX,maxY").
     * @param bounds the bounds string
     * @return Double[]{centroidX, centroidY} or {null, null} if invalid
     */
    private static Double[] computeCentroidFromBounds(String bounds) {
        if (bounds == null || bounds.isEmpty()) return new Double[]{null, null};
        String[] parts = bounds.split(",");
        if (parts.length != 4) return new Double[]{null, null};
        try {
            double minX = Double.parseDouble(parts[0]);
            double minY = Double.parseDouble(parts[1]);
            double maxX = Double.parseDouble(parts[2]);
            double maxY = Double.parseDouble(parts[3]);
            double centroidX = (minX + maxX) / 2.0;
            double centroidY = (minY + maxY) / 2.0;
            return new Double[]{centroidX, centroidY};
        } catch (NumberFormatException e) {
            return new Double[]{null, null};
        }
    }
}
