package com.smartcampus.exception;

/**
 * Custom exception wrapping SQL or persistence failures cleanly.
 * Included as a shared dependency required for Student database operations.
 */
public class DatabaseOperationException extends Exception {
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseOperationException(String message) {
        super(message);
    }
}
