package com.productivitycoach.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating a DailyLog with optional initial TimeEntries.
 */
public class DailyLogRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Valid
    private List<TimeEntryRequest> timeEntries = new ArrayList<>();

    // ---- Constructors ----

    public DailyLogRequest() {}

    public DailyLogRequest(LocalDate date, List<TimeEntryRequest> timeEntries) {
        this.date = date;
        this.timeEntries = timeEntries;
    }

    // ---- Getters & Setters ----

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<TimeEntryRequest> getTimeEntries() { return timeEntries; }
    public void setTimeEntries(List<TimeEntryRequest> timeEntries) { this.timeEntries = timeEntries; }
}
