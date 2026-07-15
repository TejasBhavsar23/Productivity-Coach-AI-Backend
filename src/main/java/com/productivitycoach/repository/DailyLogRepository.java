package com.productivitycoach.repository;

import com.productivitycoach.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyLog entity.
 */
@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    /**
     * Find a DailyLog by user and exact date.
     */
    Optional<DailyLog> findByUserIdAndDate(Long userId, LocalDate date);

    /**
     * Find a DailyLog by its ID ensuring it belongs to the requesting user.
     */
    Optional<DailyLog> findByIdAndUserId(Long id, Long userId);

    /**
     * Fetch all daily logs for a user within a date range.
     * Used for weekly report generation.
     */
    @Query("SELECT dl FROM DailyLog dl WHERE dl.user.id = :userId " +
           "AND dl.date >= :startDate AND dl.date <= :endDate " +
           "ORDER BY dl.date ASC")
    List<DailyLog> findByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Fetch all logs for a user ordered by date descending.
     */
    List<DailyLog> findByUserIdOrderByDateDesc(Long userId);
}
