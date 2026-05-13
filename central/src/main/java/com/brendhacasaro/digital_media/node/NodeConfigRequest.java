package com.brendhacasaro.digital_media.node;

import java.util.UUID;

public record NodeConfigRequest(
        String url,
        double totalCapacity,
        UUID key,
        NodeStatus status
) {
}
