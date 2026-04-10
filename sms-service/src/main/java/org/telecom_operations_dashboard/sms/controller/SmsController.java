package org.telecom_operations_dashboard.sms.controller;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.sms.service.SmsCurrentStateService;
import org.telecom_operations_dashboard.sms.service.impl.SmsRawSseBroadcaster;
import org.telecom_operations_dashboard.common.controller.AbstractSseController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Validated
public class SmsController extends AbstractSseController {

    private final SmsCurrentStateService smsCurrentStateService;
    private final SmsRawSseBroadcaster smsRawSseBroadcaster;

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawSms() {
        return smsRawSseBroadcaster.registerRawStream();
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentSms(
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) @Max(300000) long intervalMs
    ) {
        long safeInterval = Math.max(intervalMs, 1000);
        long sseTimeoutMs = Math.max(safeInterval * 3, 15_000L);
        return createScheduledEmitter(
            safeInterval,
            sseTimeoutMs,
                "sms-current",
                "/api/sms/current/stream",
            smsCurrentStateService::getCurrentSms,
            true
        );
    }
}
