package com.brendhacasaro.digital_media.node;

import java.util.UUID;

public record NodePatchRequest(
        String url,
        Double totalCapacity,
        UUID key,
        NodeStatus status
) {
}
