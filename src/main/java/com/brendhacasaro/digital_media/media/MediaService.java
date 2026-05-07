package com.brendhacasaro.digital_media.media;

import com.brendhacasaro.digital_media.media.model.Media;
import com.brendhacasaro.digital_media.node.Node;
import com.brendhacasaro.digital_media.node_media.NodeMedia;
import com.brendhacasaro.digital_media.node_media.NodeMediaRepository;
import com.brendhacasaro.digital_media.orchestrator.Orchestrator;
import com.brendhacasaro.digital_media.orchestrator.OrchestratorException;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;
    private final Orchestrator orchestrator;
    private final RestClient restClient = RestClient.create();
    private final NodeMediaRepository nodeMediaRepository;

    @Transactional
    public String createMedia(MultipartFile file) {
        Node node;
        try {
            node = orchestrator.chooseNode();
        } catch (Exception e) {
            throw new OrchestratorException("Error to choose node", e);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        // add header to send the key to node
        // add to search the key in db of node
        restClient.post()
                .uri(node.getUrl())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RestClientException("HTTP error: " + res.getStatusCode() + res.getBody());
                })
                .toBodilessEntity();

        Media media = new Media();
        media.setName(file.getOriginalFilename());
        mediaRepository.save(media);

        NodeMedia nodeMedia = new NodeMedia();
        nodeMedia.setNode(node);
        nodeMedia.setMedia(media);
        nodeMediaRepository.save(nodeMedia);

        return "/download/" + media.getId();
    }

    public Resource downloadMedia(UUID mediaId) {
        Node node = nodeMediaRepository.findNodeByMediaId(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Id" + mediaId + "not found"));

        // add header to send the key to node
        // add to search the key in db of node
        return restClient.get()
                .uri(node.getUrl() + "/" + mediaId)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw new RestClientException("HTTP error: " + response.getStatusCode());
                    }
                    return new InputStreamResource(response.getBody());
                });
    }
}