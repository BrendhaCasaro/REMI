package com.brendhacasaro.remi_node.auth;

import com.brendhacasaro.remi_node.health.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import(NodeAuthFilter.class)
class NodeAuthFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn401WhenNoAuthHeader() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenInvalidAuthHeader() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header("Authorization", "Bearer wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200WhenValidAuthHeader() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header("Authorization", "Bearer dev-key-123"))
                .andExpect(status().isOk());
    }
}
