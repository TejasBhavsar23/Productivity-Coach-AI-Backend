package com.productivitycoach.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.productivitycoach.ai.AiApiResponse;
import com.productivitycoach.ai.AiService;
import com.productivitycoach.ai.PromptTemplates;
import com.productivitycoach.dto.response.AiResponse;
import com.productivitycoach.dto.response.WeeklyReportResponse;
import com.productivitycoach.entity.DailyLog;
import com.productivitycoach.entity.Goal;
import com.productivitycoach.exception.AiServiceException;
import com.productivitycoach.exception.ResourceNotFoundException;
import com.productivitycoach.repository.DailyLogRepository;
import com.productivitycoach.repository.GoalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates all AI-powered features:
 * 1. Daily productivity analysis
 * 2. Weekly report generation
 * 3. Chat assistant with user context
 *
 * Each method:
 * - Loads the relevant data from the database
 * - Builds a prompt via {@link PromptTemplates}
 * - Calls the AI via {@link AiService}
 * - Parses and returns a structured response with token usage
 * - Delegates token persistence to {@link TokenUsageService}
 */
@Service
public class AiAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisService.class);

    private static final String ENDPOINT_ANALYZE  = "/ai/analyze/day";
    private static final String ENDPOINT_REPORT   = "/ai/report/weekly";
    private static final String ENDPOINT_CHAT     = "/ai/chat";

    private final AiService aiService;
    private final DailyLogRepository dailyLogRepository;
    private final GoalRepository goalRepository;
    private final TokenUsageService tokenUsageService;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(AiService aiService,
                              DailyLogRepository dailyLogRepository,
                              GoalRepository goalRepository,
                              TokenUsageService tokenUsageService,
                              ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.dailyLogRepository = dailyLogRepository;
        this.goalRepository = goalRepository;
        this.tokenUsageService = tokenUsageService;
        this.objectMapper = objectMapper;
    }

    // ----------------------------------------------------------------
    // 1. DAILY ANALYSIS
    // ----------------------------------------------------------------

    /**
     * Analyses a single day's log and returns a productivity breakdown.
     * The AI is asked to return structured JSON with: productivityScore,
     * timeWasteInsights, improvementSuggestions, goalAlignmentComment.
     */
    @Transactional(readOnly = true)
    public AiResponse analyzeDay(LocalDate date, Long userId) {
        logger.info("[AI-ANALYZE] userId={}, date={}", userId, date);

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new ResourceNotFoundException("DailyLog", "date", date));

        List<Goal> goals = goalRepository.findByUserId(userId);

        String prompt = PromptTemplates.buildDailyAnalysisPrompt(dailyLog, goals);
        AiApiResponse aiRaw = aiService.sendPrompt(prompt, "daily-analysis");

        // Persist token usage asynchronously-safe
        tokenUsageService.saveTokenUsage(userId, ENDPOINT_ANALYZE,
                aiRaw.getPromptTokens(), aiRaw.getCompletionTokens(), aiRaw.getTotalTokens());

        return new AiResponse(
                aiRaw.getContent(),
                aiRaw.getPromptTokens(),
                aiRaw.getCompletionTokens(),
                aiRaw.getTotalTokens()
        );
    }

    // ----------------------------------------------------------------
    // 2. WEEKLY REPORT
    // ----------------------------------------------------------------

    /**
     * Generates a weekly productivity report covering the most recent 7 days.
     * Parses the AI's structured JSON into a {@link WeeklyReportResponse}.
     */
    @Transactional(readOnly = true)
    public WeeklyReportResponse generateWeeklyReport(Long userId) {
        LocalDate endDate   = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        logger.info("[AI-WEEKLY] userId={}, range={} to {}", userId, startDate, endDate);

        List<DailyLog> weekLogs = dailyLogRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate);

        List<Goal> goals = goalRepository.findByUserId(userId);

        String prompt = PromptTemplates.buildWeeklyReportPrompt(weekLogs, goals, startDate, endDate);
        AiApiResponse aiRaw = aiService.sendPrompt(prompt, "weekly-report");

        tokenUsageService.saveTokenUsage(userId, ENDPOINT_REPORT,
                aiRaw.getPromptTokens(), aiRaw.getCompletionTokens(), aiRaw.getTotalTokens());

        return parseWeeklyReportJson(aiRaw);
    }

    // ----------------------------------------------------------------
    // 3. CHAT ASSISTANT
    // ----------------------------------------------------------------

    /**
     * Sends a user message to the AI, enriched with user goals and recent logs,
     * and returns the AI's plain-text conversational response.
     */
    @Transactional(readOnly = true)
    public AiResponse chat(String userMessage, Long userId) {
        logger.info("[AI-CHAT] userId={}", userId);

        List<Goal> goals = goalRepository.findByUserId(userId);

        // Provide up to 7 recent daily logs as context
        LocalDate today     = LocalDate.now();
        LocalDate weekAgo   = today.minusDays(7);
        List<DailyLog> recentLogs = dailyLogRepository
                .findByUserIdAndDateBetween(userId, weekAgo, today);

        String prompt = PromptTemplates.buildChatPrompt(userMessage, goals, recentLogs);
        AiApiResponse aiRaw = aiService.sendPrompt(prompt, "chat");

        tokenUsageService.saveTokenUsage(userId, ENDPOINT_CHAT,
                aiRaw.getPromptTokens(), aiRaw.getCompletionTokens(), aiRaw.getTotalTokens());

        return new AiResponse(
                aiRaw.getContent(),
                aiRaw.getPromptTokens(),
                aiRaw.getCompletionTokens(),
                aiRaw.getTotalTokens()
        );
    }

    // ----------------------------------------------------------------
    // PRIVATE: JSON PARSING FOR WEEKLY REPORT
    // ----------------------------------------------------------------

    /**
     * Parses the Claude JSON response for the weekly report.
     * Falls back to raw text if JSON parsing fails, to ensure the endpoint
     * never returns a 500 just because of a parsing issue.
     */
    private WeeklyReportResponse parseWeeklyReportJson(AiApiResponse aiRaw) {
        try {
            String content = aiRaw.getContent().trim();

            // Strip markdown code fences if the AI included them
            if (content.startsWith("```")) {
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            JsonNode root = objectMapper.readTree(content);

            double totalProductiveHours = root.path("totalProductiveHours").asDouble(0.0);
            double goalAlignmentScore   = root.path("goalAlignmentScore").asDouble(0.0);
            String summary              = root.path("summary").asText("");

            // Parse time distribution: { "Coding": 18.0, "Meetings": 6.0 }
            Map<String, Double> timeDistribution = new LinkedHashMap<>();
            JsonNode distNode = root.path("timeDistribution");
            if (distNode.isObject()) {
                distNode.fields().forEachRemaining(entry ->
                        timeDistribution.put(entry.getKey(), entry.getValue().asDouble(0.0))
                );
            }

            // Parse suggestions array
            List<String> suggestions = new ArrayList<>();
            JsonNode suggestionsNode = root.path("suggestions");
            if (suggestionsNode.isArray()) {
                suggestionsNode.forEach(node -> suggestions.add(node.asText()));
            }

            return new WeeklyReportResponse(
                    totalProductiveHours,
                    goalAlignmentScore,
                    timeDistribution,
                    suggestions,
                    summary,
                    aiRaw.getPromptTokens(),
                    aiRaw.getCompletionTokens(),
                    aiRaw.getTotalTokens()
            );

        } catch (Exception e) {
            logger.error("[AI-WEEKLY] Failed to parse JSON response, returning raw text: {}", e.getMessage());
            // Graceful fallback — return the raw text in the summary field
            return new WeeklyReportResponse(
                    0.0, 0.0,
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    aiRaw.getContent(),
                    aiRaw.getPromptTokens(),
                    aiRaw.getCompletionTokens(),
                    aiRaw.getTotalTokens()
            );
        }
    }
}
