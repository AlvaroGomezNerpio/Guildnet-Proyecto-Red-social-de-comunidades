package com.guildnet.backend.common.exception;

/** La operación entra en conflicto con el estado actual (HTTP 409), p. ej. un email ya registrado. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
