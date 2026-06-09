package com.brendhacasaro.remi_central.media;

import com.brendhacasaro.remi_central.media.model.Media;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.Orchestrator;
import com.brendhacasaro.remi_central.orchestrator.OrchestratorException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final Orchestrator orchestrator;
    private final RestClient restClient;

    @Autowired
    public MediaService(MediaRepository mediaRepository, Orchestrator orchestrator) {
        this.mediaRepository = mediaRepository;
        this.orchestrator = orchestrator;
        this.restClient = RestClient.create();
    }

    // Apenas para injeção de mock RestClient nos testes
    MediaService(MediaRepository mediaRepository, Orchestrator orchestrator, RestClient restClient) {
        this.mediaRepository = mediaRepository;
        this.orchestrator = orchestrator;
        this.restClient = restClient;
    }

    @Transactional
    public String createMedia(MultipartFile file) {
        Node node;
        try {
            node = orchestrator.chooseNode();
        } catch (Exception e) {
            throw new OrchestratorException("No Nodes available", e);
        }

        Media media = new Media(file.getOriginalFilename());
        media.setNode(node);
        mediaRepository.save(media);

        ByteArrayResource fileResource;
        try {
            fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("mediaId", media.getId().toString());

        restClient.post()
                .uri(node.getUrl() + "/api/files/upload")
                .headers(headers -> headers.setBearerAuth(node.getKey().toString()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    try (var bodyStream = res.getBody()) {
                        String errorBody = new String(bodyStream.readAllBytes());
                        throw new RestClientException("HTTP error: " + res.getStatusCode() + " " + errorBody);
                    } catch (IOException e) {
                        throw new RestClientException("HTTP error: " + res.getStatusCode());
                    }
                })
                .toBodilessEntity();

        return "/download/" + media.getId();
    }

    public Resource downloadMedia(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Id " + mediaId + " not found"));

        Node node = media.getNode();

        return restClient.get()
                .uri(node.getUrl() + "/api/files/download/" + mediaId)
                .headers(headers -> headers.setBearerAuth(node.getKey().toString()))
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw new RestClientException("Error to download media: " + response.getStatusCode());
                    }
                    return new InputStreamResource(response.getBody());
                });
    }

    @Transactional
    public void deleteMedia(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Id " + mediaId + " not found"));

        Node node = media.getNode();

        if (node != null) {
            restClient.delete()
                    .uri(node.getUrl() + "/api/files/delete/" + mediaId)
                    .headers(headers -> headers.setBearerAuth(node.getKey().toString()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RestClientException("HTTP error: " + res.getStatusCode());
                    })
                    .toBodilessEntity();
        }

        mediaRepository.delete(media);
    }

    public List<Media> getAllMedias() {
        return mediaRepository.findAll();
    }

    public Media getMediaById(UUID id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Id " + id + " not found"));
    }
}
