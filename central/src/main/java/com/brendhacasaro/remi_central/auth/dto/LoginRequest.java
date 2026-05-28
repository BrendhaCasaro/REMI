package com.brendhacasaro.remi_central.auth.dto;

public record LoginRequest(
        String username,
        String password
) {
}
