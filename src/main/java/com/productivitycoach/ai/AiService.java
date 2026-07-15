package com.productivitycoach.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.productivitycoach.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that integrates with the Claude (Anthropic) AI API using Spring WebClient.
 *
 * Responsibilities:
 * - Build the HTTP request payload for each AI call
 * - Parse the raw API response into an {@link AiApiResponse}
 * - Extract and return full token usage (prompt + completion + total)
 * - Handle all API errors gracefully with meaningful messages
 *
 * Token usage is extracted from the Claude response's "usage" field:
 * {
 *   "usage": {
 *     "input_tokens": 120,
 *     "output_tokens": 80
 *   }
 * }
 */
@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.claude.api-key}")
    private String apiKey;

    @Value("${ai.claude.base-url}")
    private String baseUrl;

    @Value("${ai.claude.model}")
    private String model;

    @Value("${ai.claude.max-tokens}")
    private int maxTokens;

    @Value("${ai.claude.api-version}")
    private String apiVersion;

    public AiService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    // ----------------------------------------------------------------
    // PUBLIC API
    // ----------------------------------------------------------------

    /**
     * Sends a prompt to the Claude API and returns the parsed response
     * including full token usage metadata.
     *
     * @param prompt  the fully-formed prompt string from {@link PromptTemplates}
     * @param context a short label used in log messages (e.g. "daily-analysis")
     * @return {@link AiApiResponse} with content text and token counts
     * @throws AiServiceException on any API error
     */
    public AiApiResponse sendPrompt(String prompt, String context) {
        logger.info("[AI] Sending {} prompt to Claude model: {}", context, model);

        Map<String, Object> requestBody = buildRequestBody(prompt);

        try {
            String rawResponse = webClient.post()
                    .uri(baseUrl + "/v1/messages")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", apiVersion)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            AiApiResponse parsed = parseClaudeResponse(rawResponse);

            logger.info("[AI] {} completed — prompt_tokens={}, completion_tokens={}, total_tokens={}",
                    context,
                    parsed.getPromptTokens(),
                    parsed.getCompletionTokens(),
                    parsed.getTotalTokens());

            return parsed;

        } catch (WebClientResponseException e) {
            logger.error("[AI] Claude API HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiServiceException(
                    "AI service returned an error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[AI] Unexpected error calling Claude API: {}", e.getMessage(), e);
            throw new AiServiceException("Unexpected error communicating with AI service: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------
    // PRIVATE HELPERS
    // ----------------------------------------------------------------

    /**
     * Constructs the Claude Messages API request body.
     * Uses the "user" role with a single text message.
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("messages", List.of(message));

        return body;
    }

    /**
     * Parses the Claude API JSON response.
     *
     * Claude response structure:
     * {
     *   "id": "msg_...",
     *   "type": "message",
     *   "content": [{ "type": "text", "text": "..." }],
     *   "usage": {
     *     "input_tokens": 120,
     *     "output_tokens": 80
     *   }
     * }
     */
    private AiApiResponse parseClaudeResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AiServiceException("AI service returned an empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            // Extract text content from the first content block
            String contentText = "";
            JsonNode contentArray = root.path("content");
            if (contentArray.isArray() && contentArray.size() > 0) {
                JsonNode firstBlock = contentArray.get(0);
                if (firstBlock.has("text")) {
                    contentText = firstBlock.get("text").asText();
                }
            }

            if (contentText.isBlank()) {
                logger.warn("[AI] Claude returned empty content. Full response: {}", rawResponse);
                throw new AiServiceException("AI returned empty content in response");
            }

            // Extract token usage from the "usage" field
            JsonNode usage = root.path("usage");
            int promptTokens     = usage.path("input_tokens").asInt(0);
            int completionTokens = usage.path("output_tokens").asInt(0);
            int totalTokens      = promptTokens + completionTokens;

            return new AiApiResponse(contentText, promptTokens, completionTokens, totalTokens);

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[AI] Failed to parse Claude response: {}", e.getMessage());
            throw new AiServiceException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }
}
