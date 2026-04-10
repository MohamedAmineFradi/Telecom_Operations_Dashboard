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

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController extends AbstractSseController {

    private final SmsCurrentStateService smsCurrentStateService;
    private final SmsRawSseBroadcaster smsRawSseBroadcaster;

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawSms() {
        return smsRawSseBroadcaster.registerRawStream();
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentSms(
            @RequestParam(name = "intervalMs", defaultValue = "5000") long intervalMs
    ) {
        return createScheduledEmitter(
                intervalMs,
                "sms-current",
                "/api/sms/current/stream",
                smsCurrentStateService::getCurrentSms
        );
    }
}
