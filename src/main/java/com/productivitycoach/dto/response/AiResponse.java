package com.productivitycoach.dto.response;

/**
 * Core AI response DTO returned by all AI endpoints.
 * Contains the AI-generated text along with full token usage metadata.
 *
 * Example:
 * {
 *   "responseText": "You should reduce entertainment time...",
 *   "promptTokens": 120,
 *   "completionTokens": 80,
 *   "totalTokens": 200
 * }
 */
public class AiResponse {

    private String responseText;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    // ---- Constructors ----

    public AiResponse() {}

    public AiResponse(String responseText, Integer promptTokens,
                      Integer completionTokens, Integer totalTokens) {
        this.responseText = responseText;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    // ---- Getters & Setters ----

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
}
