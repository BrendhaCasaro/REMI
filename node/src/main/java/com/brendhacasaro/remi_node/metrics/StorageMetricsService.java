package com.brendhacasaro.remi_node.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class StorageMetricsService {
    private final Path storageRoot;

    public StorageMetricsService(@Value("${node.storage.path:storage}") String storagePath) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public double diskUsedGb() {
        if (!Files.exists(storageRoot) || !Files.isDirectory(storageRoot)) {
            return 0.0;
        }

        try (Stream<Path> paths = Files.walk(storageRoot)) {
            long bytes = paths
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .sum();

            return bytes / (1024.0 * 1024.0 * 1024.0);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to inspect storage usage", e);
        }
    }
}
