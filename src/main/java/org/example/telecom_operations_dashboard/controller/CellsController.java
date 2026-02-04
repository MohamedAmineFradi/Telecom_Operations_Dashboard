package org.example.telecom_operations_dashboard.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.CellDetailsDto;
import org.example.telecom_operations_dashboard.service.CellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cells")
@RequiredArgsConstructor
@Validated
public class CellsController {

    private static final Logger log = LoggerFactory.getLogger(CellsController.class);

    private final CellService cellService;

    @GetMapping("/{id}")
    public ResponseEntity<CellDetailsDto> getCellDetails(@PathVariable("id") @Positive Integer id) {
        log.info("Cell details requested for {}", id);
        return ResponseEntity.ok(cellService.getCellDetails(id));
    }
}
