package com.brendhacasaro.remi_node.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MetricsResponse(@JsonProperty("disk_used") double diskUsed) {
}
