package com.guildnet.backend.common.exception;

/** El usuario autenticado no tiene permiso para realizar la operación (HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
