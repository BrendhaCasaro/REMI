package com.brendhacasaro.remi_node.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StorageMetricsService {
    private final Path storageRoot;

    public StorageMetricsService(@Value("${node.storage.path:/storage}") String storagePath) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public double diskFreeGb() {
        try {
            FileStore store = Files.getFileStore(storageRoot);
            long freeBytes = store.getUsableSpace();
            return freeBytes / (1024.0 * 1024.0 * 1024.0);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read disk free space", e);
        }
    }
}