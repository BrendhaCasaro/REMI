package com.brendhacasaro.remi_node.stored_media;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class StoredMediaController {
    private final StoredMediaService storageMediaService;

    public StoredMediaController(StoredMediaService storageMediaService) {
        this.storageMediaService = storageMediaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaId") UUID mediaId
    ) {
        String url = storageMediaService.upload(mediaId, file);
        return ResponseEntity.created(URI.create(url)).build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") UUID mediaId) {
        Resource resource = storageMediaService.download(mediaId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID mediaId) {
        storageMediaService.delete(mediaId);
        return ResponseEntity.noContent().build();
    }
}
