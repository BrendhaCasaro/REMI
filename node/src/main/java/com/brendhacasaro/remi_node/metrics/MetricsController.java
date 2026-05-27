package com.brendhacasaro.remi_node.metrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetricsController {
    private final StorageMetricsService storageMetricsService;

    public MetricsController(StorageMetricsService storageMetricsService) {
        this.storageMetricsService = storageMetricsService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> metrics() {
        return ResponseEntity.ok(new MetricsResponse(storageMetricsService.diskUsedGb()));
    }
}
