package com.brendhacasaro.digital_media.media.service;

import com.brendhacasaro.digital_media.node_media.NodeMedia;
import com.brendhacasaro.digital_media.node_media.NodeMediaRepository;
import com.brendhacasaro.digital_media.media.model.Media;
import com.brendhacasaro.digital_media.media.repository.MediaRepository;
import com.brendhacasaro.digital_media.node.Node;
import com.brendhacasaro.digital_media.orchestrator.Orchestrator;
import com.brendhacasaro.digital_media.orchestrator.OrchestratorException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;
    private final Orchestrator orchestrator;
    private final RestClient restClient = RestClient.create();
    private final NodeMediaRepository nodeMediaRepository;

    public void createMedia(MultipartFile file) {
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
                    throw new RuntimeException("HTTP error: " + res.getStatusCode() + res.getBody());
                })
                .toBodilessEntity();

        Media media = new Media();
        media.setName(file.getOriginalFilename());
        mediaRepository.save(media);

        NodeMedia nodeMedia = new NodeMedia();
        nodeMedia.setNode(node);
        nodeMedia.setMedia(media);
        nodeMediaRepository.save(nodeMedia);
    }

    // Endpoint de download retorna MultipartFile,
    // tipo inadequado para resposta HTTP de arquivo; ideal é Resource/stream + headers corretos.
    public Resource downloadMedia(UUID idMedia) {

    }
}