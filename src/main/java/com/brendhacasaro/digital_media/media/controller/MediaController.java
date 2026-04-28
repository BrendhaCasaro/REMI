package com.brendhacasaro.digital_media.media.controller;

import com.brendhacasaro.digital_media.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/videos")
public class MediaController {
    private final MediaService mediaService;

    @PostMapping
    public ResponseEntity<?> uploadMedia(MultipartFile media) {
    }
}
