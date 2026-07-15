package com.productivitycoach.dto.response;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for the weekly AI report endpoint.
 * Includes time distribution, goal alignment, suggestions, and token usage.
 */
public class WeeklyReportResponse {

    private double totalProductiveHours;
    private double goalAlignmentScore;
    private Map<String, Double> timeDistribution;   // category -> hours
    private List<String> suggestions;
    private String summary;

    // ---- Token usage ----
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    // ---- Constructors ----

    public WeeklyReportResponse() {}

    public WeeklyReportResponse(double totalProductiveHours, double goalAlignmentScore,
                                 Map<String, Double> timeDistribution, List<String> suggestions,
                                 String summary, Integer promptTokens,
                                 Integer completionTokens, Integer totalTokens) {
        this.totalProductiveHours = totalProductiveHours;
        this.goalAlignmentScore = goalAlignmentScore;
        this.timeDistribution = timeDistribution;
        this.suggestions = suggestions;
        this.summary = summary;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    // ---- Getters & Setters ----

    public double getTotalProductiveHours() { return totalProductiveHours; }
    public void setTotalProductiveHours(double totalProductiveHours) { this.totalProductiveHours = totalProductiveHours; }

    public double getGoalAlignmentScore() { return goalAlignmentScore; }
    public void setGoalAlignmentScore(double goalAlignmentScore) { this.goalAlignmentScore = goalAlignmentScore; }

    public Map<String, Double> getTimeDistribution() { return timeDistribution; }
    public void setTimeDistribution(Map<String, Double> timeDistribution) { this.timeDistribution = timeDistribution; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
}
