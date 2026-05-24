package com.brendhacasaro.remi_central.node;

import java.util.UUID;

public record NodePatchRequest(
        String url,
        Double totalCapacity,
        UUID key,
        NodeStatus status
) {
}
