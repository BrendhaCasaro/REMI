package com.brendhacasaro.remi_central.node.dto;

import com.brendhacasaro.remi_central.node.NodeStatus;

import java.util.UUID;

public record NodeConfigRequest(
        String url,
        Double totalCapacity,
        UUID key,
        NodeStatus status
) {
}
