package com.brendhacasaro.remi_central.user.dto;

import com.brendhacasaro.remi_central.user.model.Role;

public record UserRequest(
        String username,
        String password,
        Role role
) {
}
