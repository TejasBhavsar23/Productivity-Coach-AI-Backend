package com.productivitycoach.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when communication with the AI provider (Claude / OpenAI) fails.
 * Examples: network timeout, invalid API key, provider rate limit exceeded.
 * Maps to HTTP 502 Bad Gateway — the upstream AI service is the broken dependency.
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
