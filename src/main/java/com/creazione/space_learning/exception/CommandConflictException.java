package com.creazione.space_learning.exception;

public class CommandConflictException extends RuntimeException {
    public CommandConflictException(String message) {
        super(message);
    }

    public CommandConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
