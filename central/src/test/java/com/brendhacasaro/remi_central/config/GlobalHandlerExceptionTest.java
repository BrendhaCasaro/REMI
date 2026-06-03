package com.brendhacasaro.remi_central.config;

import com.brendhacasaro.remi_central.orchestrator.OrchestratorException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalHandlerExceptionTest {

    private final GlobalHandlerException handler = new GlobalHandlerException();

    @Test
    void handleEntityNotFoundException_shouldReturn404() {
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(
                new EntityNotFoundException("Not found"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().message());
    }

    @Test
    void handleMultipartException_shouldReturn400() {
        ResponseEntity<ErrorResponse> response = handler.handleMultipartException(
                new MultipartException("Bad request"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Bad request", response.getBody().message());
    }

    @Test
    void handleRestClientException_shouldReturn500() {
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(
                new RestClientException("Connection failed"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Connection failed", response.getBody().message());
    }

    @Test
    void handleOrchestratorException_shouldReturn500() {
        ResponseEntity<ErrorResponse> response = handler.handleOrchestratorException(
                new OrchestratorException("No nodes", new RuntimeException()));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("No nodes", response.getBody().message());
    }
}
