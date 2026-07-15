package com.productivitycoach.repository;

import com.productivitycoach.entity.Goal;
import com.productivitycoach.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Goal entity.
 * Includes queries needed to enforce the one-PRIMARY-goal-per-user business rule.
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Fetch all goals belonging to a specific user.
     */
    List<Goal> findByUserId(Long userId);

    /**
     * Find a specific goal by its ID and owning user (prevents cross-user access).
     */
    Optional<Goal> findByIdAndUserId(Long id, Long userId);

    /**
     * Check if a user already has a goal with the given priority.
     * Used to enforce the single-PRIMARY constraint.
     */
    boolean existsByUserIdAndPriority(Long userId, Priority priority);

    /**
     * Find the existing PRIMARY goal for a user (used during update validation).
     */
    Optional<Goal> findByUserIdAndPriority(Long userId, Priority priority);

    /**
     * Count goals by user ID and priority level.
     */
    long countByUserIdAndPriority(Long userId, Priority priority);
}
