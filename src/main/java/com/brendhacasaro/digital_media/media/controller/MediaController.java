package com.brendhacasaro.digital_media.media.controller;

import com.brendhacasaro.digital_media.media.service.MediaService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/videos")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@RequestParam MultipartFile file) {
        mediaService.createMedia(file);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadMedia(@PathVariable UUID id) {
        return ResponseEntity.ok(mediaService.downloadMedia(id));
    }
}
