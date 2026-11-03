package com.guildnet.backend.common.exception;

/** La petición no es válida (HTTP 400). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
