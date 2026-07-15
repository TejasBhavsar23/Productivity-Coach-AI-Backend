package com.productivitycoach.dto.response;

import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Response DTO for a single TimeEntry record.
 */
public class TimeEntryResponse {

    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private String activity;
    private String category;
    private Integer productivityScore;
    private LocalDateTime createdAt;

    // ---- Constructors ----

    public TimeEntryResponse() {}

    public TimeEntryResponse(Long id, LocalTime startTime, LocalTime endTime,
                             String activity, String category,
                             Integer productivityScore, LocalDateTime createdAt) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activity = activity;
        this.category = category;
        this.productivityScore = productivityScore;
        this.createdAt = createdAt;
    }

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
