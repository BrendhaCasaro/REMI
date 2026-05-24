package com.brendhacasaro.remi.node;

public record NodeResponse(
        String url,
        Double totalCapacity,
        NodeStatus status
) {
}
