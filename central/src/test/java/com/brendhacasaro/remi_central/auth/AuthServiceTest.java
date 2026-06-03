package com.brendhacasaro.remi_central.auth;

import com.brendhacasaro.remi_central.auth.dto.LoginRequest;
import com.brendhacasaro.remi_central.auth.dto.LoginResponse;
import com.brendhacasaro.remi_central.user.UserRepository;
import com.brendhacasaro.remi_central.user.model.Role;
import com.brendhacasaro.remi_central.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken() {
        User user = new User("admin", "encodedPass", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("admin", "rawPass"));

        assertEquals("jwt-token", response.token());
        verify(userRepository).findByUsername("admin");
        verify(passwordEncoder).matches("rawPass", "encodedPass");
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("unknown", "pass")));
    }

    @Test
    void login_shouldThrowWhenPasswordMismatch() {
        User user = new User("admin", "encodedPass", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("admin", "wrongPass")));
    }
}
