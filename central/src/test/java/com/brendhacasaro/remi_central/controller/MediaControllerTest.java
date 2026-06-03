package com.brendhacasaro.remi_central.controller;

import com.brendhacasaro.remi_central.auth.JwtAuthenticationFilter;
import com.brendhacasaro.remi_central.auth.SecurityConfig;
import com.brendhacasaro.remi_central.config.TestSecurityConfig;
import com.brendhacasaro.remi_central.media.MediaController;
import com.brendhacasaro.remi_central.media.MediaService;
import com.brendhacasaro.remi_central.media.dto.MediaResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MediaController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthenticationFilter.class}))
@Import(TestSecurityConfig.class)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @Test
    void uploadMedia_shouldReturn201WithLocation() throws Exception {
        when(mediaService.createMedia(any())).thenReturn("/download/" + UUID.randomUUID());

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void getAllMedias_shouldReturn200() throws Exception {
        when(mediaService.getAllMedias()).thenReturn(
                List.of(new MediaResponse("a.txt"), new MediaResponse("b.txt")));

        mockMvc.perform(get("/api/files/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("a.txt"));
    }

    @Test
    void deleteMedia_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/files/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}
