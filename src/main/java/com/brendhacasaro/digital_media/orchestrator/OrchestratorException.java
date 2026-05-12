package com.brendhacasaro.digital_media.orchestrator;

public class OrchestratorException extends RuntimeException {
    public OrchestratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrchestratorException(String message) {
        super(message);
    }
}
