package org.telecom_operations_dashboard.call.mapper;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class CallMapper {
    public BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
    
}
