package com.brendhacasaro.remi_node.stored_media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoredMediaController.class)
class StoredMediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoredMediaService storedMediaService;

    @Test
    void upload_shouldReturn201() throws Exception {
        UUID mediaId = UUID.randomUUID();
        when(storedMediaService.upload(any(), any()))
                .thenReturn("/api/files/download/" + mediaId);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("mediaId", mediaId.toString())
                        .header("Authorization", "Bearer dev-key-123"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/files/download/" + mediaId));
    }

    @Test
    void download_shouldReturn200() throws Exception {
        UUID mediaId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        when(storedMediaService.download(mediaId)).thenReturn(file.getResource());

        mockMvc.perform(get("/api/files/download/{id}", mediaId)
                .header("Authorization", "Bearer dev-key-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID mediaId = UUID.randomUUID();
        doNothing().when(storedMediaService).delete(mediaId);

        mockMvc.perform(delete("/api/files/delete/{id}", mediaId)
                .header("Authorization", "Bearer dev-key-123"))
                .andExpect(status().isNoContent());
    }
}
