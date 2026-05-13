package com.brendhacasaro.digital_media.media;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/videos")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMedia(@RequestParam MultipartFile file) {
        String url = mediaService.createMedia(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create(url))
                .build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadMedia(@PathVariable UUID id) {
        Resource media = mediaService.downloadMedia(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment, filename=\"" + media.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(media);
    }
}
