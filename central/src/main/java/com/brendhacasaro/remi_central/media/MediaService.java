package com.brendhacasaro.remi_central.media;

import com.brendhacasaro.remi_central.media.model.Media;
import com.brendhacasaro.remi_central.node.model.Node;
import com.brendhacasaro.remi_central.orchestrator.Orchestrator;
import com.brendhacasaro.remi_central.orchestrator.OrchestratorException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;
    private final Orchestrator orchestrator;
    private final RestClient restClient = RestClient.create();

    @Transactional
    public String createMedia(MultipartFile file) {
        Node node;
        try {
            node = orchestrator.chooseNode();
        } catch (Exception e) {
            throw new OrchestratorException("No Nodes available", e);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        restClient.post()
                .uri(node.getUrl() + "/api/files/upload")
                .headers(headers -> headers.setBearerAuth(node.getKey().toString()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RestClientException("HTTP error: " + res.getStatusCode() + res.getBody());
                });

        Media media = new Media(file.getOriginalFilename());
        media.setNode(node);
        mediaRepository.save(media);

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
                        throw new RestClientException("HTTP error: " + response.getStatusCode());
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
}
