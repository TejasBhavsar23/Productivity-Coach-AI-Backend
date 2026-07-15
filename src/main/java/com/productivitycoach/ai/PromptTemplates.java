package com.productivitycoach.ai;

import com.productivitycoach.entity.DailyLog;
import com.productivitycoach.entity.Goal;
import com.productivitycoach.entity.TimeEntry;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Centralised prompt template factory.
 *
 * All AI prompts live here so they can be reviewed, versioned, and tuned
 * independently of business logic. Each method returns a fully-formed
 * prompt string ready to be sent to the AI provider.
 *
 * Prompts instruct the AI to return ONLY valid JSON so that responses
 * can be parsed deterministically.
 */
public final class PromptTemplates {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Prevent instantiation — this is a utility class
    private PromptTemplates() {}

    // ----------------------------------------------------------------
    // 1. DAILY ANALYSIS PROMPT
    // ----------------------------------------------------------------

    /**
     * Builds the prompt for analysing a single day's productivity.
     *
     * Expected JSON response shape:
     * {
     *   "productivityScore": 3.8,
     *   "timeWasteInsights": ["...", "..."],
     *   "improvementSuggestions": ["...", "..."],
     *   "goalAlignmentComment": "..."
     * }
     */
    public static String buildDailyAnalysisPrompt(DailyLog dailyLog, List<Goal> goals) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are an expert productivity coach. Analyze the following daily time log ");
        sb.append("and the user's goals, then return ONLY a valid JSON object — no markdown, ");
        sb.append("no extra text.\n\n");

        sb.append("DATE: ").append(dailyLog.getDate().format(DATE_FMT)).append("\n\n");

        sb.append("USER GOALS:\n");
        if (goals.isEmpty()) {
            sb.append("  No goals set yet.\n");
        } else {
            for (Goal g : goals) {
                sb.append("  - [").append(g.getPriority()).append("] ")
                  .append(g.getTitle());
                if (g.getDeadline() != null) {
                    sb.append(" (deadline: ").append(g.getDeadline().format(DATE_FMT)).append(")");
                }
                sb.append("\n");
            }
        }

        sb.append("\nTIME ENTRIES:\n");
        List<TimeEntry> entries = dailyLog.getTimeEntries();
        if (entries.isEmpty()) {
            sb.append("  No time entries recorded for this day.\n");
        } else {
            for (TimeEntry te : entries) {
                sb.append("  - ").append(te.getStartTime())
                  .append(" to ").append(te.getEndTime())
                  .append(" | ").append(te.getCategory())
                  .append(" | ").append(te.getActivity())
                  .append(" | Score: ").append(te.getProductivityScore()).append("/5\n");
            }
        }

        sb.append("\nReturn ONLY this JSON structure:\n");
        sb.append("{\n");
        sb.append("  \"productivityScore\": <float 1.0-5.0>,\n");
        sb.append("  \"timeWasteInsights\": [\"<insight1>\", \"<insight2>\"],\n");
        sb.append("  \"improvementSuggestions\": [\"<suggestion1>\", \"<suggestion2>\"],\n");
        sb.append("  \"goalAlignmentComment\": \"<one paragraph>\"\n");
        sb.append("}");

        return sb.toString();
    }

    // ----------------------------------------------------------------
    // 2. WEEKLY REPORT PROMPT
    // ----------------------------------------------------------------

    /**
     * Builds the prompt for a full 7-day productivity report.
     *
     * Expected JSON response shape:
     * {
     *   "totalProductiveHours": 32.5,
     *   "goalAlignmentScore": 4.1,
     *   "timeDistribution": { "Coding": 18.0, "Meetings": 6.0, ... },
     *   "suggestions": ["...", "..."],
     *   "summary": "..."
     * }
     */
    public static String buildWeeklyReportPrompt(List<DailyLog> weekLogs, List<Goal> goals,
                                                  LocalDate startDate, LocalDate endDate) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are an expert productivity coach. Analyze the following weekly time logs ");
        sb.append("and return ONLY a valid JSON object — no markdown, no extra text.\n\n");

        sb.append("WEEK: ").append(startDate.format(DATE_FMT))
          .append(" to ").append(endDate.format(DATE_FMT)).append("\n\n");

        sb.append("USER GOALS:\n");
        if (goals.isEmpty()) {
            sb.append("  No goals set.\n");
        } else {
            for (Goal g : goals) {
                sb.append("  - [").append(g.getPriority()).append("] ").append(g.getTitle()).append("\n");
            }
        }

        sb.append("\nDAILY BREAKDOWN:\n");
        for (DailyLog log : weekLogs) {
            sb.append("  ").append(log.getDate().format(DATE_FMT)).append(":\n");
            for (TimeEntry te : log.getTimeEntries()) {
                sb.append("    * ").append(te.getStartTime())
                  .append("-").append(te.getEndTime())
                  .append(" | ").append(te.getCategory())
                  .append(" | ").append(te.getActivity())
                  .append(" | Score: ").append(te.getProductivityScore()).append("/5\n");
            }
            if (log.getTimeEntries().isEmpty()) {
                sb.append("    (no entries)\n");
            }
        }

        sb.append("\nReturn ONLY this JSON structure:\n");
        sb.append("{\n");
        sb.append("  \"totalProductiveHours\": <float>,\n");
        sb.append("  \"goalAlignmentScore\": <float 1.0-5.0>,\n");
        sb.append("  \"timeDistribution\": { \"<category>\": <hours>, ... },\n");
        sb.append("  \"suggestions\": [\"<tip1>\", \"<tip2>\", \"<tip3>\"],\n");
        sb.append("  \"summary\": \"<two paragraph executive summary>\"\n");
        sb.append("}");

        return sb.toString();
    }

    // ----------------------------------------------------------------
    // 3. CHAT ASSISTANT PROMPT
    // ----------------------------------------------------------------

    /**
     * Builds the system context + user message for the AI chat assistant.
     * Injects user goals and recent activity as context so the AI can
     * give personalised, grounded advice.
     */
    public static String buildChatPrompt(String userMessage, List<Goal> goals,
                                          List<DailyLog> recentLogs) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a knowledgeable, empathetic AI productivity coach. ");
        sb.append("Use the user's goals and recent activity below as context. ");
        sb.append("Give specific, actionable, personalised advice. ");
        sb.append("Respond in plain conversational text (not JSON).\n\n");

        sb.append("USER GOALS:\n");
        if (goals.isEmpty()) {
            sb.append("  No goals set yet.\n");
        } else {
            for (Goal g : goals) {
                sb.append("  - [").append(g.getPriority()).append("] ").append(g.getTitle());
                if (g.getDescription() != null && !g.getDescription().isBlank()) {
                    sb.append(": ").append(g.getDescription());
                }
                sb.append("\n");
            }
        }

        sb.append("\nRECENT ACTIVITY (last ").append(recentLogs.size()).append(" days):\n");
        if (recentLogs.isEmpty()) {
            sb.append("  No recent logs found.\n");
        } else {
            for (DailyLog log : recentLogs) {
                sb.append("  ").append(log.getDate().format(DATE_FMT)).append(": ");
                long entryCount = log.getTimeEntries().size();
                double avgScore = log.getTimeEntries().stream()
                        .mapToInt(TimeEntry::getProductivityScore)
                        .average()
                        .orElse(0.0);
                sb.append(entryCount).append(" activities, avg score ")
                  .append(String.format("%.1f", avgScore)).append("/5\n");
            }
        }

        sb.append("\nUSER MESSAGE:\n").append(userMessage);

        return sb.toString();
    }
}
