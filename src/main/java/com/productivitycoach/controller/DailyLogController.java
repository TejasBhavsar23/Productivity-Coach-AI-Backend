package com.productivitycoach.controller;

import com.productivitycoach.dto.request.DailyLogRequest;
import com.productivitycoach.dto.response.DailyLogResponse;
import com.productivitycoach.security.SecurityUtils;
import com.productivitycoach.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for daily time tracking.
 *
 * POST   /logs           — Create a DailyLog (with optional time entries)
 * GET    /logs/{date}    — Retrieve a log by date (format: yyyy-MM-dd)
 * PUT    /logs/{id}      — Update an existing log (replaces time entries)
 * DELETE /logs/{id}      — Delete a log and all its time entries
 */
@RestController
@RequestMapping("/logs")
@Tag(name = "Daily Logs", description = "Track daily time blocks and productivity scores")
@SecurityRequirement(name = "BearerAuth")
public class DailyLogController {

    private static final Logger logger = LoggerFactory.getLogger(DailyLogController.class);

    private final DailyLogService dailyLogService;

    public DailyLogController(DailyLogService dailyLogService) {
        this.dailyLogService = dailyLogService;
    }

    @PostMapping
    @Operation(summary = "Create a daily log",
               description = "Creates a new log for a given date. Only one log per date is allowed.")
    public ResponseEntity<DailyLogResponse> createLog(@Valid @RequestBody DailyLogRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("POST /logs - userId={}, date={}", userId, request.getDate());
        DailyLogResponse response = dailyLogService.createLog(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{date}")
    @Operation(summary = "Get log by date",
               description = "Retrieves the daily log for a specific date. Date format: yyyy-MM-dd")
    public ResponseEntity<DailyLogResponse> getLogByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("GET /logs/{} - userId={}", date, userId);
        return ResponseEntity.ok(dailyLogService.getLogByDate(date, userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a daily log",
               description = "Replaces all time entries for the given log ID")
    public ResponseEntity<DailyLogResponse> updateLog(@PathVariable Long id,
                                                       @Valid @RequestBody DailyLogRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("PUT /logs/{} - userId={}", id, userId);
        return ResponseEntity.ok(dailyLogService.updateLog(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a daily log",
               description = "Deletes the log and all associated time entries")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("DELETE /logs/{} - userId={}", id, userId);
        dailyLogService.deleteLog(id, userId);
        return ResponseEntity.noContent().build();
    }
}
