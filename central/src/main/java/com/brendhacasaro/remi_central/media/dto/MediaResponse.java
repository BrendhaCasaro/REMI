package com.brendhacasaro.remi_central.media.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        String name,
        LocalDateTime createdAt
) {
}
