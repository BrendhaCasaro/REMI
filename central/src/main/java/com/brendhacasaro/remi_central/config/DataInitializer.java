package com.brendhacasaro.remi_central.config;

import com.brendhacasaro.remi_central.user.UserRepository;
import com.brendhacasaro.remi_central.user.UserService;
import com.brendhacasaro.remi_central.user.dto.UserRequest;
import com.brendhacasaro.remi_central.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) return;

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            userService.createUser(new UserRequest(adminUsername, adminPassword, Role.ADMIN));
        }
    }
}
