package com.hackathon.backend.common.exception;

public class NoAvailableAgentException extends RuntimeException {

    public NoAvailableAgentException(String message) {
        super(message);
    }
}
