package com.brendhacasaro.remi_central.node;

import java.util.UUID;

public record NodeConfigRequest(
        String url,
        Double totalCapacity,
        UUID key,
        NodeStatus status
) {
}
