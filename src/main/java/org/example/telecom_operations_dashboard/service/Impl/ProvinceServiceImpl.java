package org.example.telecom_operations_dashboard.service.Impl;

import org.example.telecom_operations_dashboard.controller.exception.ResourceNotFoundException;
import org.example.telecom_operations_dashboard.dto.ProvinceDto;
import org.example.telecom_operations_dashboard.model.Province;
import org.example.telecom_operations_dashboard.repository.ProvinceRepository;
import org.example.telecom_operations_dashboard.service.ProvinceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinceServiceImpl implements ProvinceService {

    private static final Logger log = LoggerFactory.getLogger(ProvinceServiceImpl.class);

    private final ProvinceRepository provinceRepository;

    public ProvinceServiceImpl(ProvinceRepository provinceRepository) {
        this.provinceRepository = provinceRepository;
    }

    @Override
    public List<ProvinceDto> getAllProvinces() {
        List<Province> provinces = provinceRepository.findAllOrderByProvincia();
        log.info("Fetched {} provinces", provinces.size());
        return provinces.stream()
                .map(p -> new ProvinceDto(
                        p.getProvincia(),
                        p.getPopulation()
                ))
                .toList();
    }

    @Override
    public ProvinceDto getProvinceDetails(String provincia) {
        Province province = provinceRepository.findById(provincia)
                .orElseThrow(() -> new ResourceNotFoundException("Province not found: " + provincia));
        log.info("Province details requested: provincia={}", provincia);
        return new ProvinceDto(
                province.getProvincia(),
                province.getPopulation()
        );
    }
}
