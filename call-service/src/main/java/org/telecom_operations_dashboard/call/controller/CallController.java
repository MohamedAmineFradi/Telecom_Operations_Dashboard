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

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallController extends AbstractSseController {

    private final CallCurrentStateService callCurrentStateService;
    private final CallRawSseBroadcaster callRawSseBroadcaster;

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawCall() {
        return callRawSseBroadcaster.registerRawStream();
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentCall(
            @RequestParam(name = "intervalMs", defaultValue = "60000") long intervalMs
    ) {
        return createScheduledEmitter(
                intervalMs,
                "call-current",
                "/api/call/current/stream",
                callCurrentStateService::getCurrentCalls
        );
    }
}
