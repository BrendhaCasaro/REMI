package com.brendhacasaro.remi_central.auth;

import com.brendhacasaro.remi_central.user.model.Role;
import com.brendhacasaro.remi_central.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-test-key");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 864000000L);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        User user = new User("testuser", "password", Role.ADMIN);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_shouldReturnSubject() {
        User user = new User("admin", "pass", Role.ADMIN);

        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);

        assertEquals("admin", username);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        User user = new User("validuser", "pass", Role.USER);
        String token = jwtService.generateToken(user);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("validuser")
                .password("pass")
                .authorities("ROLE_USER")
                .build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_shouldReturnFalseForWrongUser() {
        User user = new User("user1", "pass", Role.USER);
        String token = jwtService.generateToken(user);
        UserDetails wrongUser = org.springframework.security.core.userdetails.User
                .withUsername("user2")
                .password("pass")
                .authorities("ROLE_USER")
                .build();

        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }
}
