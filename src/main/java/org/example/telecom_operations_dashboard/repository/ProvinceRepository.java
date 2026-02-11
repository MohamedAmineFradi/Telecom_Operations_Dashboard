package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<Province, String> {

    @Query("SELECT p FROM Province p ORDER BY p.provincia ASC")
    List<Province> findAllOrderByProvincia();
}
