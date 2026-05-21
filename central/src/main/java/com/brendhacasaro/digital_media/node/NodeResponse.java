package com.brendhacasaro.digital_media.node;

public record NodeResponse(
        String url,
        Double totalCapacity,
        NodeStatus status
) {
}
