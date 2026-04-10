package org.telecom_operations_dashboard.internet.controller;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.internet.service.InternetCurrentStateService;
import org.telecom_operations_dashboard.internet.service.impl.InternetRawSseBroadcaster;
import org.telecom_operations_dashboard.common.controller.AbstractSseController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/internet")
@RequiredArgsConstructor
public class InternetController extends AbstractSseController {

    private final InternetCurrentStateService internetCurrentStateService;
    private final InternetRawSseBroadcaster internetRawSseBroadcaster;

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawInternet() {
        return internetRawSseBroadcaster.registerRawStream();
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentInternet(
            @RequestParam(name = "intervalMs", defaultValue = "60000") long intervalMs
    ) {
        return createScheduledEmitter(
                intervalMs,
                "internet-current",
                "/api/internet/current/stream",
                internetCurrentStateService::getCurrentInternet
        );
    }
}
