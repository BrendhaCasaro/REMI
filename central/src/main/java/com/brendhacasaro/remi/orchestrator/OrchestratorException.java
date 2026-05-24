package com.brendhacasaro.remi.orchestrator;

public class OrchestratorException extends RuntimeException {
    public OrchestratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrchestratorException(String message) {
        super(message);
    }
}
