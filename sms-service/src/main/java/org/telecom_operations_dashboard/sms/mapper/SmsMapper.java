package org.telecom_operations_dashboard.sms.mapper;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class SmsMapper {
    public BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalSms(BigDecimal in, BigDecimal out) {
        return safe(in).add(safe(out));
    }
}
