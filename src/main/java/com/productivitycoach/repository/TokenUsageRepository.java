package com.productivitycoach.repository;

import com.productivitycoach.entity.TokenUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for TokenUsage entity.
 * Supports cost analytics and per-user token monitoring.
 */
@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsage, Long> {

    /**
     * Get all token usage records for a specific user.
     */
    List<TokenUsage> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Get token usage records for a user within a specific time window.
     */
    @Query("SELECT tu FROM TokenUsage tu WHERE tu.user.id = :userId " +
           "AND tu.timestamp >= :from AND tu.timestamp <= :to")
    List<TokenUsage> findByUserIdAndTimestampBetween(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Sum total tokens consumed by a user for analytics/billing.
     */
    @Query("SELECT SUM(tu.totalTokens) FROM TokenUsage tu WHERE tu.user.id = :userId")
    Long sumTotalTokensByUserId(@Param("userId") Long userId);

    /**
     * Group token usage by endpoint for a user.
     */
    @Query("SELECT tu.endpoint, SUM(tu.totalTokens) FROM TokenUsage tu " +
           "WHERE tu.user.id = :userId GROUP BY tu.endpoint")
    List<Object[]> sumTokensByEndpointForUser(@Param("userId") Long userId);
}
