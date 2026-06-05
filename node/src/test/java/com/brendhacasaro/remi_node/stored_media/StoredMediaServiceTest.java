package com.brendhacasaro.remi_node.stored_media;

import com.brendhacasaro.remi_node.stored_media.model.StoredMedia;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoredMediaServiceTest {

    @Mock
    private StoredMediaRepository repository;

    private StoredMediaService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new StoredMediaService(repository);
        ReflectionTestUtils.setField(service, "storageRoot", tempDir);
    }

    @Test
    void upload_shouldSaveFileAndRecord() throws IOException {
        UUID mediaId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello".getBytes());

        when(repository.existsById(mediaId)).thenReturn(false);

        String url = service.upload(mediaId, file);

        assertTrue(Files.exists(tempDir.resolve("test_txt_" + mediaId)));
        assertEquals("hello", Files.readString(tempDir.resolve("test_txt_" + mediaId)));
        assertEquals("/api/files/download/" + mediaId, url);
        verify(repository).save(any(StoredMedia.class));
    }

    @Test
    void upload_shouldThrowWhenFileNull() {
        UUID mediaId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> service.upload(mediaId, null));
        verifyNoInteractions(repository);
    }

    @Test
    void upload_shouldThrowWhenFileEmpty() {
        UUID mediaId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> service.upload(mediaId, file));
        verifyNoInteractions(repository);
    }

    @Test
    void upload_shouldThrowWhenMediaIdAlreadyExists() {
        UUID mediaId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        when(repository.existsById(mediaId)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class,
                () -> service.upload(mediaId, file));
        verify(repository, never()).save(any());
    }

    @Test
    void upload_shouldSanitizeFilename() throws IOException {
        UUID mediaId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file", "../bad<name>.txt", "text/plain", "content".getBytes());

        when(repository.existsById(mediaId)).thenReturn(false);

        service.upload(mediaId, file);

        assertTrue(Files.exists(tempDir.resolve("__bad_name_.txt_" + mediaId)));
    }

    @Test
    void upload_shouldUseDefaultNameWhenOriginalIsNull() throws IOException {
        UUID mediaId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile(
                "file", (String) null, "text/plain", "content".getBytes());

        when(repository.existsById(mediaId)).thenReturn(false);

        service.upload(mediaId, file);

        assertTrue(Files.exists(tempDir.resolve("file_" + mediaId)));
    }

    @Test
    void download_shouldReturnResourceWhenFound() throws IOException {
        UUID mediaId = UUID.randomUUID();
        Path storedFile = tempDir.resolve("existing_file.txt_" + mediaId);
        Files.writeString(storedFile, "data");

        when(repository.findById(mediaId))
                .thenReturn(Optional.of(new StoredMedia(mediaId, storedFile.toString())));

        var resource = service.download(mediaId);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals("data", new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    void download_shouldThrowWhenNotFound() {
        UUID mediaId = UUID.randomUUID();

        when(repository.findById(mediaId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.download(mediaId));
    }

    @Test
    void download_shouldThrowWhenFileNotExistsOnDisk() {
        UUID mediaId = UUID.randomUUID();
        String fakePath = tempDir.resolve("nonexistent.txt_" + mediaId).toString();

        when(repository.findById(mediaId))
                .thenReturn(Optional.of(new StoredMedia(mediaId, fakePath)));

        assertThrows(EntityNotFoundException.class,
                () -> service.download(mediaId));
    }

    @Test
    void delete_shouldRemoveFileAndRecord() throws IOException {
        UUID mediaId = UUID.randomUUID();
        Path storedFile = tempDir.resolve("to_delete.txt_" + mediaId);
        Files.writeString(storedFile, "data");

        when(repository.findById(mediaId))
                .thenReturn(Optional.of(new StoredMedia(mediaId, storedFile.toString())));

        service.delete(mediaId);

        assertFalse(Files.exists(storedFile));
        verify(repository).deleteById(mediaId);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        UUID mediaId = UUID.randomUUID();

        when(repository.findById(mediaId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(mediaId));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void delete_shouldSucceedWhenFileAlreadyDeleted() throws IOException {
        UUID mediaId = UUID.randomUUID();
        String fakePath = tempDir.resolve("already_deleted.txt_" + mediaId).toString();

        when(repository.findById(mediaId))
                .thenReturn(Optional.of(new StoredMedia(mediaId, fakePath)));

        service.delete(mediaId);

        verify(repository).deleteById(mediaId);
    }
}
