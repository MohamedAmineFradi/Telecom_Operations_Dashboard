package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByTimestampAfterOrderByTimestampDesc(OffsetDateTime since);

    List<Alert> findAllByOrderByTimestampDesc();

    Page<Alert> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<Alert> findByTimestampAfterOrderByTimestampDesc(OffsetDateTime since, Pageable pageable);

    boolean existsByCellIdAndTypeAndTimestamp(Integer cellId, String type, OffsetDateTime timestamp);
}
