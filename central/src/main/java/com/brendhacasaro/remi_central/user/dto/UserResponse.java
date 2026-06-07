package com.brendhacasaro.remi_central.user.dto;

import com.brendhacasaro.remi_central.user.model.Role;

public record UserResponse(
        Integer id,
        String username,
        Role role
) {
}
