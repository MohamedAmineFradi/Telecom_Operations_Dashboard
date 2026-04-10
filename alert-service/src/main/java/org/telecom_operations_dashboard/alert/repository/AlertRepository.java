package org.telecom_operations_dashboard.alert.repository;

import org.telecom_operations_dashboard.alert.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByTimestampAfterOrderByTimestampDesc(OffsetDateTime since);

    List<Alert> findAllByOrderByTimestampDesc();

    Page<Alert> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<Alert> findByTimestampAfterOrderByTimestampDesc(OffsetDateTime since, Pageable pageable);

    Optional<Alert> findFirstByCellIdAndTypeAndTimestamp(Integer cellId, String type, OffsetDateTime timestamp);

    Page<Alert> findBySeverityIgnoreCaseOrderByTimestampDesc(String severity, Pageable pageable);
}
