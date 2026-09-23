package com.example.observability.controller;

import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
@CrossOrigin(origins = "*")
public class LogController {

    private static final Logger log = LoggerFactory.getLogger("ClientAppLogger");

    @PostMapping
    public ResponseEntity<Void> ingestLog(@RequestBody LogPayload payload) {
        String structuredLog = String.format(
            "level=%s appVersion=%s userId=%s msg=%s",
            payload.level(),
            payload.appVersion() != null ? payload.appVersion() : "unknown",
            payload.userId() != null ? payload.userId() : "anonymous",
            payload.message()
        );

        switch (payload.level().toUpperCase()) {
            case "ERROR" -> log.error(structuredLog);
            case "WARN"  -> log.warn(structuredLog);
            default      -> log.info(structuredLog);
        }

        return ResponseEntity.accepted().build();
    }
}
