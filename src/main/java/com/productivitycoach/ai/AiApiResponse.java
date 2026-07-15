package com.productivitycoach.ai;

/**
 * Internal DTO that holds the raw text and token counts extracted from
 * a Claude or OpenAI API response.
 *
 * This object is used internally by {@link AiService} and is never
 * returned directly to the client — it gets mapped to the public
 * {@link com.productivitycoach.dto.response.AiResponse} DTO.
 */
public class AiApiResponse {

    private String content;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    // ---- Constructors ----

    public AiApiResponse() {}

    public AiApiResponse(String content, int promptTokens, int completionTokens, int totalTokens) {
        this.content = content;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    // ---- Getters & Setters ----

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }

    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }

    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }

    @Override
    public String toString() {
        return "AiApiResponse{promptTokens=" + promptTokens +
               ", completionTokens=" + completionTokens +
               ", totalTokens=" + totalTokens + "}";
    }
}
