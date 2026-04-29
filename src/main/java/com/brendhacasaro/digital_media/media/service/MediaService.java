package com.brendhacasaro.digital_media.media.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.brendhacasaro.digital_media.media.model.Media;
import com.brendhacasaro.digital_media.media.repository.MediaRepository;
import com.brendhacasaro.digital_media.orchestrator.Orchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;
    private final Orchestrator orchestrator;
    private final RestClient restClient = RestClient.create();

    public void createMedia(MultipartFile file) {
        String urlNode;
        try {
            urlNode = orchestrator.chooseNode();
        } catch (Exception e) {
            throw new orchestratorException("Error to send the media to node");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        restClient.post()
                .uri(urlNode)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new RuntimeException("HTTP error: " + res.getStatusCode());
                })
                .toBodilessEntity();

        Media media = new Media();
        media.setUrlNode(urlNode);
        media.setName(file.getName());

        mediaRepository.save(media);
    }
}