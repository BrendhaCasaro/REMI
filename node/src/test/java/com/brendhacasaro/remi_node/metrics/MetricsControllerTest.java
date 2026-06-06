package com.brendhacasaro.remi_node.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageMetricsService storageMetricsService;

    @Test
    void metrics_shouldReturnDiskFree() throws Exception {
        when(storageMetricsService.diskFreeGb()).thenReturn(42.5);

        mockMvc.perform(get("/api/metrics")
                .header("Authorization", "Bearer dev-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disk_free").value(42.5));
    }
}
