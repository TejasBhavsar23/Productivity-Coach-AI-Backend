package com.productivitycoach.controller;

import com.productivitycoach.dto.request.AnalyzeDayRequest;
import com.productivitycoach.dto.request.ChatRequest;
import com.productivitycoach.dto.response.AiResponse;
import com.productivitycoach.dto.response.WeeklyReportResponse;
import com.productivitycoach.security.SecurityUtils;
import com.productivitycoach.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for all AI-powered endpoints.
 *
 * POST /ai/analyze/day   — Analyse a specific day's productivity
 * GET  /ai/report/weekly — Generate a 7-day productivity report
 * POST /ai/chat          — Chat with the AI productivity coach
 *
 * All responses include token usage metadata (promptTokens, completionTokens, totalTokens).
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI Analysis", description = "AI-powered productivity analysis, reports, and chat")
@SecurityRequirement(name = "BearerAuth")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final AiAnalysisService aiAnalysisService;

    public AiController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * Analyses a specific day's time entries against the user's goals.
     *
     * Response includes:
     * - productivityScore
     * - timeWasteInsights
     * - improvementSuggestions
     * - goalAlignmentComment
     * - Token usage breakdown
     */
    @PostMapping("/analyze/day")
    @Operation(
        summary = "Analyse a day's productivity",
        description = "Uses AI to analyse your time log for the given date against your goals. " +
                      "Returns insights, suggestions, and token usage."
    )
    public ResponseEntity<AiResponse> analyzeDay(@Valid @RequestBody AnalyzeDayRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("POST /ai/analyze/day - userId={}, date={}", userId, request.getDate());
        AiResponse response = aiAnalysisService.analyzeDay(request.getDate(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a comprehensive weekly productivity report.
     *
     * Covers the last 7 days and returns:
     * - Total productive hours
     * - Time distribution by category
     * - Goal alignment score
     * - Improvement suggestions
     * - Token usage breakdown
     */
    @GetMapping("/report/weekly")
    @Operation(
        summary = "Generate a weekly report",
        description = "Generates an AI-powered weekly productivity report covering the last 7 days. " +
                      "Includes time distribution, goal alignment, suggestions, and token usage."
    )
    public ResponseEntity<WeeklyReportResponse> getWeeklyReport() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("GET /ai/report/weekly - userId={}", userId);
        WeeklyReportResponse response = aiAnalysisService.generateWeeklyReport(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a message to the AI coach.
     *
     * The AI receives:
     * - The user's message
     * - Their active goals as context
     * - Recent 7-day activity summary as context
     *
     * Returns a conversational response plus token usage.
     */
    @PostMapping("/chat")
    @Operation(
        summary = "Chat with AI coach",
        description = "Send a message to your AI productivity coach. " +
                      "The AI uses your goals and recent logs to give personalised advice."
    )
    public ResponseEntity<AiResponse> chat(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("POST /ai/chat - userId={}", userId);
        AiResponse response = aiAnalysisService.chat(request.getMessage(), userId);
        return ResponseEntity.ok(response);
    }
}
