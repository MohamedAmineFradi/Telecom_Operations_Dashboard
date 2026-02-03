package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByTimestampAfterOrderByTimestampDesc(OffsetDateTime since);

    List<Alert> findAllByOrderByTimestampDesc();
}
