package org.telecom_operations_dashboard.call.controller;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.call.service.CallCurrentStateService;
import org.telecom_operations_dashboard.call.service.impl.CallRawSseBroadcaster;
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
@RequestMapping("/api/call")
@RequiredArgsConstructor
@Validated
public class CallController extends AbstractSseController {

    private final CallCurrentStateService callCurrentStateService;
    private final CallRawSseBroadcaster callRawSseBroadcaster;

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawCall() {
        return callRawSseBroadcaster.registerRawStream();
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentCall(
            @RequestParam(name = "intervalMs", defaultValue = "5000") @Min(1000) @Max(300000) long intervalMs
    ) {
        long safeInterval = Math.max(intervalMs, 1000);
        long sseTimeoutMs = Math.max(safeInterval * 3, 15_000L);
        return createScheduledEmitter(
            safeInterval,
            sseTimeoutMs,
                "call-current",
                "/api/call/current/stream",
            callCurrentStateService::getCurrentCalls,
            true
        );
    }
}
