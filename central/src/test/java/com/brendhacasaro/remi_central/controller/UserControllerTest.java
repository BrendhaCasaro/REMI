package com.brendhacasaro.remi_central.controller;

import com.brendhacasaro.remi_central.auth.JwtAuthenticationFilter;
import com.brendhacasaro.remi_central.auth.SecurityConfig;
import com.brendhacasaro.remi_central.config.TestSecurityConfig;
import com.brendhacasaro.remi_central.user.UserController;
import com.brendhacasaro.remi_central.user.UserService;
import com.brendhacasaro.remi_central.user.dto.UserRequest;
import com.brendhacasaro.remi_central.user.dto.UserResponse;
import com.brendhacasaro.remi_central.user.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_shouldReturn201() throws Exception {
        when(userService.createUser(any())).thenReturn(new UserResponse(1, "newuser", Role.ADMIN));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRequest("newuser", "pass", Role.ADMIN))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void getAllUsers_shouldReturn200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponse(1, "user1", Role.ADMIN),
                new UserResponse(2, "user2", Role.ADMIN)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    void getUserById_shouldReturn200() throws Exception {
        when(userService.getUserById(1)).thenReturn(new UserResponse(1, "found", Role.ADMIN));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("found"));
    }

    @Test
    void updateUser_shouldReturn200() throws Exception {
        when(userService.updateUser(any(), any())).thenReturn(new UserResponse(1, "updated", Role.ADMIN));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRequest("updated", "pass", Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
