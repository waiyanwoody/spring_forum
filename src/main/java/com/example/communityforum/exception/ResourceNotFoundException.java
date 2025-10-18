package com.example.communityforum.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
