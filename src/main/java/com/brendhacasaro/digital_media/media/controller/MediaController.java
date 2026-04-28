package com.brendhacasaro.digital_media.media.controller;

import com.brendhacasaro.digital_media.media.model.Media;
import com.brendhacasaro.digital_media.media.service.MediaService;
import com.brendhacasaro.digital_media.orchestrator.Orchestrator;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@AllArgsConstructor
@RequestMapping("/videos")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping
    public ResponseEntity<?> uploadMedia(@RequestParam MultipartFile file) {
        return ResponseEntity.ok();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadMedia(@PathVariable UUID id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }
}
