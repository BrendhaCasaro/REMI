package com.brendhacasaro.remi_central.node.dto;

import com.brendhacasaro.remi_central.node.NodeStatus;

public record NodePatchRequest(
        String url,
        Double totalCapacity,
        String key,
        NodeStatus status,
        Double diskFree
) {
}
