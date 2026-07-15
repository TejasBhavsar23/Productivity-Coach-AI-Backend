package com.productivitycoach.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for a DailyLog with its nested TimeEntries.
 */
public class DailyLogResponse {

    private Long id;
    private LocalDate date;
    private List<TimeEntryResponse> timeEntries = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- Constructors ----

    public DailyLogResponse() {}

    public DailyLogResponse(Long id, LocalDate date,
                            List<TimeEntryResponse> timeEntries,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.date = date;
        this.timeEntries = timeEntries;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<TimeEntryResponse> getTimeEntries() { return timeEntries; }
    public void setTimeEntries(List<TimeEntryResponse> timeEntries) { this.timeEntries = timeEntries; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
