package org.telecom_operations_dashboard.cell.repository;

import org.telecom_operations_dashboard.cell.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, String> {

    @Query("SELECT p FROM Province p ORDER BY p.provincia ASC")
    List<Province> findAllOrderByProvincia();
}
