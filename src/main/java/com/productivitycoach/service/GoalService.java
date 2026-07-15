package com.productivitycoach.service;

import com.productivitycoach.dto.request.GoalRequest;
import com.productivitycoach.dto.response.GoalResponse;
import com.productivitycoach.entity.Goal;
import com.productivitycoach.entity.Priority;
import com.productivitycoach.entity.User;
import com.productivitycoach.exception.BadRequestException;
import com.productivitycoach.exception.ResourceNotFoundException;
import com.productivitycoach.repository.GoalRepository;
import com.productivitycoach.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for Goal management.
 *
 * Key invariant enforced here:
 *   A user may have AT MOST ONE goal with priority = PRIMARY.
 *   Attempting to create/update to a second PRIMARY goal throws {@link BadRequestException}.
 */
@Service
public class GoalService {

    private static final Logger logger = LoggerFactory.getLogger(GoalService.class);

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    @Transactional
    public GoalResponse createGoal(GoalRequest request, Long userId) {
        logger.info("Creating goal for userId={}, priority={}", userId, request.getPriority());

        User user = findUser(userId);

        // Enforce: only one PRIMARY goal per user
        if (request.getPriority() == Priority.PRIMARY) {
            if (goalRepository.existsByUserIdAndPriority(userId, Priority.PRIMARY)) {
                throw new BadRequestException(
                        "You already have a PRIMARY goal. " +
                        "Please update or delete it before creating a new one.");
            }
        }

        Goal goal = new Goal(
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getDeadline(),
                user
        );

        Goal saved = goalRepository.save(goal);
        logger.info("Goal created: id={}, title='{}', userId={}", saved.getId(), saved.getTitle(), userId);

        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // READ ALL
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<GoalResponse> getAllGoals(Long userId) {
        return goalRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    @Transactional
    public GoalResponse updateGoal(Long goalId, GoalRequest request, Long userId) {
        logger.info("Updating goalId={} for userId={}", goalId, userId);

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        // If changing priority TO PRIMARY, check no other PRIMARY exists
        if (request.getPriority() == Priority.PRIMARY
                && goal.getPriority() != Priority.PRIMARY) {
            if (goalRepository.existsByUserIdAndPriority(userId, Priority.PRIMARY)) {
                throw new BadRequestException(
                        "You already have a PRIMARY goal. " +
                        "Only one PRIMARY goal is allowed per user.");
            }
        }

        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setPriority(request.getPriority());
        goal.setDeadline(request.getDeadline());

        Goal updated = goalRepository.save(goal);
        logger.info("Goal updated: id={}", updated.getId());

        return toResponse(updated);
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        logger.info("Deleting goalId={} for userId={}", goalId, userId);

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        goalRepository.delete(goal);
        logger.info("Goal deleted: id={}", goalId);
    }

    // ----------------------------------------------------------------
    // INTERNAL HELPERS
    // ----------------------------------------------------------------

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Maps a Goal entity to a GoalResponse DTO.
     * Deliberately does not expose the User entity to the API layer.
     */
    public GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getPriority(),
                goal.getDeadline(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
