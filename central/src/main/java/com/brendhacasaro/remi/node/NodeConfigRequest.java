package com.brendhacasaro.remi.node;

import java.util.UUID;

public record NodeConfigRequest(
        String url,
        Double totalCapacity,
        UUID key,
        NodeStatus status
) {
}
