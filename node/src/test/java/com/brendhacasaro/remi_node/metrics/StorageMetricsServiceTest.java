package com.brendhacasaro.remi_node.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageMetricsServiceTest {

    @Test
    void diskFreeGb_shouldReturnPositiveValue(@TempDir Path tempDir) {
        StorageMetricsService service = new StorageMetricsService("storage");
        ReflectionTestUtils.setField(service, "storageRoot", tempDir);

        double result = service.diskFreeGb();

        assertTrue(result > 0);
    }

    @Test
    void constructor_shouldResolveToAbsolutePath() {
        StorageMetricsService service = new StorageMetricsService("storage");
        Path storageRoot = (Path) ReflectionTestUtils.getField(service, "storageRoot");

        assertNotNull(storageRoot);
        assertTrue(storageRoot.isAbsolute());
        assertTrue(storageRoot.toString().endsWith("storage"));
    }

    @Test
    void diskFreeGb_shouldThrowWhenIoException() throws Exception {
        StorageMetricsService service = new StorageMetricsService("storage");
        Path mockPath = mock();
        ReflectionTestUtils.setField(service, "storageRoot", mockPath);

        try (var filesStatic = mockStatic(Files.class)) {
            filesStatic.when(() -> Files.getFileStore(mockPath)).thenThrow(new IOException("Disk error"));

            assertThrows(UncheckedIOException.class, service::diskFreeGb);
        }
    }
}
