package org.example.producer.controller;

import lombok.RequiredArgsConstructor;
import org.example.producer.service.TimeAuthorityService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/time")
@RequiredArgsConstructor
public class TimeController {

    private final TimeAuthorityService timeAuthorityService;

    public record AuthoritativeTimeResponse(String timestamp, String source) {
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthoritativeTimeResponse> getAuthoritativeTime() {
        TimeAuthorityService.AuthoritativeTime authoritativeTime = timeAuthorityService.getAuthoritativeTime();
        return ResponseEntity.ok(new AuthoritativeTimeResponse(
            authoritativeTime.timestamp(),
            authoritativeTime.source()
        ));
    }
}
