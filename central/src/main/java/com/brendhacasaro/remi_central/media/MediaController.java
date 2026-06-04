package com.brendhacasaro.remi_central.media;

import com.brendhacasaro.remi_central.media.dto.MediaResponse;
import com.brendhacasaro.remi_central.media.model.Media;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/files")
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

    @GetMapping("/")
    public ResponseEntity<List<MediaResponse>> getAllMedias() {
        List<MediaResponse> medias = mediaService.getAllMedias().stream()
                .map(m -> new MediaResponse(m.getId(), m.getName(), m.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(medias);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
