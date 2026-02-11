package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.ProvinceDto;

import java.util.List;

public interface ProvinceService {
    List<ProvinceDto> getAllProvinces();
    ProvinceDto getProvinceDetails(String provincia);
}
