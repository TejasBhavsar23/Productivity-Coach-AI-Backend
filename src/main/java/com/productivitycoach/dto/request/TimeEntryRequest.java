package com.productivitycoach.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

/**
 * DTO for creating or updating a TimeEntry.
 */
public class TimeEntryRequest {

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Activity description is required")
    @Size(min = 2, max = 300, message = "Activity must be between 2 and 300 characters")
    private String activity;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 100, message = "Category must be between 2 and 100 characters")
    private String category;

    @NotNull(message = "Productivity score is required")
    @Min(value = 1, message = "Productivity score must be at least 1")
    @Max(value = 5, message = "Productivity score must be at most 5")
    private Integer productivityScore;

    // ---- Constructors ----

    public TimeEntryRequest() {}

    public TimeEntryRequest(LocalTime startTime, LocalTime endTime, String activity,
                            String category, Integer productivityScore) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.activity = activity;
        this.category = category;
        this.productivityScore = productivityScore;
    }

    // ---- Getters & Setters ----

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getProductivityScore() { return productivityScore; }
    public void setProductivityScore(Integer productivityScore) { this.productivityScore = productivityScore; }
}
