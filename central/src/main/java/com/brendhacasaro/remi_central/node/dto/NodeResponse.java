package com.brendhacasaro.remi_central.node.dto;

import com.brendhacasaro.remi_central.node.NodeStatus;

public record NodeResponse(
        String url,
        Double totalCapacity,
        NodeStatus status
) {
}
