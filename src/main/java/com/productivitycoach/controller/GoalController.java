package com.productivitycoach.controller;

import com.productivitycoach.dto.request.GoalRequest;
import com.productivitycoach.dto.response.GoalResponse;
import com.productivitycoach.security.SecurityUtils;
import com.productivitycoach.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Goal management.
 *
 * All endpoints require a valid JWT Bearer token.
 *
 * POST   /goals        — Create a new goal
 * GET    /goals        — List all goals for the authenticated user
 * PUT    /goals/{id}   — Update an existing goal
 * DELETE /goals/{id}   — Delete a goal
 */
@RestController
@RequestMapping("/goals")
@Tag(name = "Goals", description = "Manage personal productivity goals")
@SecurityRequirement(name = "BearerAuth")
public class GoalController {

    private static final Logger logger = LoggerFactory.getLogger(GoalController.class);

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    @Operation(summary = "Create a goal",
               description = "Only ONE PRIMARY goal is allowed per user. " +
                             "Throws 400 if a PRIMARY goal already exists.")
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("POST /goals - userId={}, priority={}", userId, request.getPriority());
        GoalResponse response = goalService.createGoal(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all goals", description = "Returns all goals for the authenticated user")
    public ResponseEntity<List<GoalResponse>> getAllGoals() {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("GET /goals - userId={}", userId);
        return ResponseEntity.ok(goalService.getAllGoals(userId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a goal", description = "Updates title, description, priority, or deadline")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id,
                                                    @Valid @RequestBody GoalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("PUT /goals/{} - userId={}", id, userId);
        GoalResponse response = goalService.updateGoal(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a goal", description = "Permanently removes a goal")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("DELETE /goals/{} - userId={}", id, userId);
        goalService.deleteGoal(id, userId);
        return ResponseEntity.noContent().build();
    }
}
