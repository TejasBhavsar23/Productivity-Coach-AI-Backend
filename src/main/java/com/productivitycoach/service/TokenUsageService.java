package com.productivitycoach.service;

import com.productivitycoach.entity.TokenUsage;
import com.productivitycoach.entity.User;
import com.productivitycoach.exception.ResourceNotFoundException;
import com.productivitycoach.repository.TokenUsageRepository;
import com.productivitycoach.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saves AI token usage records after every AI API call.
 * Enables per-user cost analytics and usage monitoring.
 */
@Service
public class TokenUsageService {

    private static final Logger logger = LoggerFactory.getLogger(TokenUsageService.class);

    private final TokenUsageRepository tokenUsageRepository;
    private final UserRepository userRepository;

    public TokenUsageService(TokenUsageRepository tokenUsageRepository,
                             UserRepository userRepository) {
        this.tokenUsageRepository = tokenUsageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Persists a token usage record for auditing and analytics.
     *
     * @param userId           the authenticated user
     * @param endpoint         the API endpoint that triggered the AI call (e.g. "/ai/analyze/day")
     * @param promptTokens     tokens used in the prompt
     * @param completionTokens tokens generated in the response
     * @param totalTokens      total tokens billed
     */
    @Transactional
    public void saveTokenUsage(Long userId, String endpoint,
                               int promptTokens, int completionTokens, int totalTokens) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            TokenUsage usage = new TokenUsage(endpoint, promptTokens, completionTokens, totalTokens, user);
            tokenUsageRepository.save(usage);

            logger.info("[TOKEN] Saved usage for userId={}, endpoint={}, total={}",
                    userId, endpoint, totalTokens);

        } catch (Exception e) {
            // Token usage saving must NEVER cause the main AI response to fail.
            // Log the error and continue.
            logger.error("[TOKEN] Failed to save token usage for userId={}: {}", userId, e.getMessage());
        }
    }
}
