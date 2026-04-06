package org.telecom_operations_dashboard.cell.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.cell.CellDetailsDto;
import org.telecom_operations_dashboard.common.dto.cell.GridCellDto;
import org.telecom_operations_dashboard.cell.service.CellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cells")
@RequiredArgsConstructor
@Validated
@ConditionalOnProperty(name = "app.service", havingValue = "cell")
public class CellsController {

    private static final Logger log = LoggerFactory.getLogger(CellsController.class);

    private final CellService cellService;

    @GetMapping
    public ResponseEntity<List<GridCellDto>> getAllCells() {
        log.info("All grid cells requested");
        return ResponseEntity.ok(cellService.getAllGridCells());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<GridCellDto>> getAllCellsPaged(Pageable pageable) {
        log.info("Paginated grid cells requested with page size {}", pageable.getPageSize());
        return ResponseEntity.ok(cellService.getAllGridCells(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CellDetailsDto> getCellDetails(@PathVariable("id") @Positive Integer id) {
        log.info("Cell details requested for {}", id);
        return ResponseEntity.ok(cellService.getCellDetails(id));
    }
}
