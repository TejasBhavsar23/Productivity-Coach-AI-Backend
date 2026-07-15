package com.productivitycoach.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for requesting an AI analysis of a specific day.
 */
public class AnalyzeDayRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    // ---- Constructors ----

    public AnalyzeDayRequest() {}

    public AnalyzeDayRequest(LocalDate date) {
        this.date = date;
    }

    // ---- Getters & Setters ----

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
