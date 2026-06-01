package com.brendhacasaro.remi_node.stored_media;

import com.brendhacasaro.remi_node.stored_media.model.StoredMedia;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoredMediaService {
    private final StoredMediaRepository storedMediarepository;
    private final Path storageRoot = Path.of("/storage");

    @Transactional
    public String upload(UUID mediaId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (storedMediarepository.existsById(mediaId)) {
            throw new DataIntegrityViolationException("Media id " + mediaId + " already exists");
        }

        String safeName = sanitizeFileName(file.getOriginalFilename());
        String finalFileName = safeName + "_" + mediaId;
        Path destination = storageRoot.resolve(finalFileName).normalize();

        if (!destination.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid destination path");
        }

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }

        storedMediarepository.save(new StoredMedia(mediaId, destination.toString()));
        return "/api/files/download/" + mediaId;
    }

    public Resource download(UUID mediaId) {
        StoredMedia storedMedia = storedMediarepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media id " + mediaId + " not found"));

        Path file = Path.of(storedMedia.getFilePath()).toAbsolutePath().normalize();
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            throw new EntityNotFoundException("File for media id " + mediaId + " not found");
        }

        return new FileSystemResource(file);
    }

    @Transactional
    public void delete(UUID mediaId) {
        StoredMedia storedMedia = storedMediarepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media id " + mediaId + " not found"));

        Path file = Path.of(storedMedia.getFilePath()).toAbsolutePath().normalize();
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file", e);
        }

        storedMediarepository.deleteById(mediaId);
    }

    private String sanitizeFileName(String originalName) {
        String name = originalName == null ? "file" : originalName;
        String sanitized = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.isBlank()) {
            return "file";
        }
        return sanitized.toLowerCase(Locale.ROOT);
    }
}
