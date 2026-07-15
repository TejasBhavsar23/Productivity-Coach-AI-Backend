package com.productivitycoach.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for sending a message to the AI chat assistant.
 */
public class ChatRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
    private String message;

    // ---- Constructors ----

    public ChatRequest() {}

    public ChatRequest(String message) {
        this.message = message;
    }

    // ---- Getters & Setters ----

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
