package com.example.communityforum.api.controller;
// Custom exception
public class FileValidationException extends RuntimeException {
    public FileValidationException(String message) {
        super(message);
    }
}

