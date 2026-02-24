package com.hms.backend.exception;

public class DuplicateServiceException extends RuntimeException {
    public DuplicateServiceException(String message) {
        super(message);
    }
}
