package com.brendhacasaro.remi_node.metrics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MetricsResponse(@JsonProperty("disk_used") double diskUsed) {
}
