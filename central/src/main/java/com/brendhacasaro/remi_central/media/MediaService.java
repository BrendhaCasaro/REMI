package com.brendhacasaro.remi_central.media;

import com.brendhacasaro.remi_central.media.dto.MediaResponse;
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

        // add header to send the key to node
        // add to search the key in db of node
        restClient.post()
                .uri(node.getUrl() + "/api/files/upload")
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

        // add header to send the key to node
        // add to search the key in db of node
        return restClient.get()
                .uri(node.getUrl() + "/api/files/download/" + mediaId)
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
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RestClientException("HTTP error: " + res.getStatusCode());
                    })
                    .toBodilessEntity();
        }

        mediaRepository.delete(media);
    }

    public List<MediaResponse> getAllMedias() {
        return mediaRepository.findAll()
                .stream()
                .map(media -> new MediaResponse(
                        media.getId(),
                        media.getName(),
                        media.getCreatedAt()
                ))
                .toList();
    }
}
