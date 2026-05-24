package com.brendhacasaro.remi_central.node;

public record NodeResponse(
        String url,
        Double totalCapacity,
        NodeStatus status
) {
}
