package com.brendhacasaro.remi_central.media;

import com.brendhacasaro.remi_central.media.model.Media;
import com.brendhacasaro.remi_central.node.NodeStatus;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.Orchestrator;
import com.brendhacasaro.remi_central.orchestrator.OrchestratorException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private Orchestrator orchestrator;

    @Mock
    private RestClient restClient;

    private MediaService mediaService;
    private Node testNode;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository, orchestrator, restClient);
        testNode = new Node("http://node:8080", 100.0, "key", NodeStatus.ONLINE, 50.0);
    }

    @Test
    void createMedia_shouldReturnDownloadUrl() {
        when(orchestrator.chooseNode()).thenReturn(testNode);
        when(mediaRepository.save(any())).thenAnswer(i -> {
            Media m = i.getArgument(0);
            var idField = Media.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(m, UUID.randomUUID());
            return m;
        });

        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class, RETURNS_SELF);
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.post()).thenReturn(postSpec);
        when(postSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        String result = mediaService.createMedia(file);

        assertTrue(result.startsWith("/download/"));
        verify(orchestrator).chooseNode();
        verify(mediaRepository).save(any());
        verify(restClient).post();
    }

    @Test
    void createMedia_shouldThrowWhenNoNodeAvailable() {
        when(orchestrator.chooseNode()).thenThrow(new OrchestratorException("No Nodes available"));

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        OrchestratorException ex = assertThrows(OrchestratorException.class, () -> mediaService.createMedia(file));
        assertEquals("No Nodes available", ex.getMessage());
        verify(orchestrator).chooseNode();
        verifyNoInteractions(mediaRepository);
    }

    @Test
    void createMedia_shouldThrowWhenNodeReturnsError() {
        when(orchestrator.chooseNode()).thenReturn(testNode);
        when(mediaRepository.save(any())).thenAnswer(i -> {
            Media m = i.getArgument(0);
            var idField = Media.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(m, UUID.randomUUID());
            return m;
        });

        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class, RETURNS_SELF);
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.post()).thenReturn(postSpec);
        when(postSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenThrow(new RestClientException("HTTP error: 500"));

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        assertThrows(RestClientException.class, () -> mediaService.createMedia(file));
    }

    @Test
    void downloadMedia_shouldReturnResource() {
        UUID mediaId = UUID.randomUUID();
        Media media = new Media("test.txt");
        media.setNode(testNode);
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class, RETURNS_SELF);
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.exchange(any())).thenReturn(
                new InputStreamResource(new ByteArrayInputStream("content".getBytes())));

        Resource result = mediaService.downloadMedia(mediaId);

        assertNotNull(result);
        verify(mediaRepository).findById(mediaId);
        verify(restClient).get();
    }

    @Test
    void downloadMedia_shouldThrowWhenMediaNotFound() {
        UUID mediaId = UUID.randomUUID();
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> mediaService.downloadMedia(mediaId));
    }

    @Test
    void downloadMedia_shouldThrowWhenExchangeFails() {
        UUID mediaId = UUID.randomUUID();
        Media media = new Media("test.txt");
        media.setNode(testNode);
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class, RETURNS_SELF);
        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.exchange(any())).thenThrow(new RestClientException("HTTP error: 500"));

        assertThrows(RestClientException.class, () -> mediaService.downloadMedia(mediaId));
    }

    @Test
    void deleteMedia_shouldDeleteFromNodeAndDb() {
        UUID mediaId = UUID.randomUUID();
        Media media = new Media("test.txt");
        media.setNode(testNode);
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

        RestClient.RequestHeadersUriSpec deleteSpec = mock(RestClient.RequestHeadersUriSpec.class, RETURNS_SELF);
        RestClient.ResponseSpec responseSpec = mock();
        when(restClient.delete()).thenReturn(deleteSpec);
        when(deleteSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        mediaService.deleteMedia(mediaId);

        verify(restClient).delete();
        verify(mediaRepository).delete(media);
    }

    @Test
    void deleteMedia_shouldDeleteFromDbEvenWhenNodeIsNull() {
        UUID mediaId = UUID.randomUUID();
        Media media = new Media("test.txt");
        media.setNode(null);
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

        mediaService.deleteMedia(mediaId);

        verify(mediaRepository).delete(media);
        verifyNoInteractions(restClient);
    }

    @Test
    void deleteMedia_shouldThrowWhenMediaNotFound() {
        UUID mediaId = UUID.randomUUID();
        when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> mediaService.deleteMedia(mediaId));
    }

    @Test
    void getAllMedias_shouldReturnAll() {
        Media m1 = new Media("a.txt");
        Media m2 = new Media("b.txt");
        when(mediaRepository.findAll()).thenReturn(List.of(m1, m2));

        List<Media> result = mediaService.getAllMedias();

        assertEquals(2, result.size());
        verify(mediaRepository).findAll();
    }
}
