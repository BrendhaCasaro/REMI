package com.brendhacasaro.remi_central.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MetricsResponse(@JsonProperty("disk_free") Double diskFree) {
}
