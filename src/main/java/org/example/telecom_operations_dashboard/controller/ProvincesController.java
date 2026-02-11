package org.example.telecom_operations_dashboard.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.ProvinceDto;
import org.example.telecom_operations_dashboard.service.ProvinceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/provinces")
@RequiredArgsConstructor
@Validated
public class ProvincesController {

    private static final Logger log = LoggerFactory.getLogger(ProvincesController.class);

    private final ProvinceService provinceService;

    @GetMapping
    public ResponseEntity<List<ProvinceDto>> getAllProvinces() {
        log.info("All provinces requested");
        return ResponseEntity.ok(provinceService.getAllProvinces());
    }

    @GetMapping("/{provincia}")
    public ResponseEntity<ProvinceDto> getProvinceDetails(@PathVariable("provincia") @NotBlank String provincia) {
        log.info("Province details requested for {}", provincia);
        return ResponseEntity.ok(provinceService.getProvinceDetails(provincia));
    }
}
