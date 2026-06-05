package com.brendhacasaro.remi_node.config;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartException;

import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalHandlerExceptionTest {

    private final GlobalHandlerException handler = new GlobalHandlerException();

    @Test
    void handleEntityNotFound_shouldReturn404() {
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(
                new EntityNotFoundException("Media not found"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Media not found", response.getBody().message());
    }

    @Test
    void handleBadRequest_shouldReturn400() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
                new IllegalArgumentException("Invalid argument"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid argument", response.getBody().message());
    }

    @Test
    void handleConflict_shouldReturn409() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(
                new DataIntegrityViolationException("Duplicate entry"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Duplicate entry", response.getBody().message());
    }

    @Test
    void handleIo_shouldReturn500() {
        ResponseEntity<ErrorResponse> response = handler.handleIo(
                new UncheckedIOException(new java.io.IOException("Disk full")));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Disk full", response.getBody().message());
    }

    @Test
    void handleGeneric_shouldReturn500() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(
                new RuntimeException("Unexpected error"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody().message());
    }

    @Test
    void handleMultipartException_shouldReturn400() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
                new MultipartException("Upload failed"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Upload failed", response.getBody().message());
    }
}
