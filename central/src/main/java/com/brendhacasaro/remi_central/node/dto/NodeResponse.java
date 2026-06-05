package com.brendhacasaro.remi_central.node.dto;

import com.brendhacasaro.remi_central.node.NodeStatus;

public record NodeResponse(
        Integer id,
        String url,
        Double totalCapacity,
        NodeStatus status
) {
}
