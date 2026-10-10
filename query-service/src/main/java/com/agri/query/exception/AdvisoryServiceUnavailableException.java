package com.agri.query.exception;

public class AdvisoryServiceUnavailableException extends RuntimeException {
    public AdvisoryServiceUnavailableException(String message) {
        super(message);
    }

    public AdvisoryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
