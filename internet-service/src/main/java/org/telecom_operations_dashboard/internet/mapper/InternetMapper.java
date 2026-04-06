package org.telecom_operations_dashboard.internet.mapper;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class InternetMapper {
    public BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
