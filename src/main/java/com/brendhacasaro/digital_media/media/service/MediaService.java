package com.brendhacasaro.digital_media.media.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.brendhacasaro.digital_media.media.model.Media;
import com.brendhacasaro.digital_media.media.repository.MediaRepository;
import com.brendhacasaro.digital_media.orchestrator.Orchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;
    private final Orchestrator orchestrator;

    public void createMedia(MultipartFile file) {
        String urlNode;
        try {
            urlNode = orchestrator.chooseNode();
        } catch (Exception e) {
            throw new orchestratorException("Error to send the media to node");
        }
        //http request

        Media media = new Media();
        media.setUrlNode(urlNode);
        media.setName(file.getName());

        mediaRepository.save(media);
    }
}