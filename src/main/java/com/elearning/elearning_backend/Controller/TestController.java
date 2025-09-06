package com.elearning.elearning_backend.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Migration System is working!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "SUCCESS");
        return response;
    }

    @GetMapping("/migration-status")
    public Map<String, Object> migrationStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("migrationSystemEnabled", true);
        response.put("environment", "development");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
            "health", "/api/migration/actuator/health",
            "metrics", "/api/migration/actuator/metrics",
            "stats", "/api/migration/stats",
            "execute", "/api/migration/execute"
        ));
        return response;
    }
} 