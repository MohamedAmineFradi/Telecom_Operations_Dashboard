package org.telecom_operations_dashboard.cell.service;

import org.telecom_operations_dashboard.common.dto.province.ProvinceDto;

import java.util.List;

public interface ProvinceService {
    List<ProvinceDto> getAllProvinces();
    ProvinceDto getProvinceDetails(String provincia);
}
