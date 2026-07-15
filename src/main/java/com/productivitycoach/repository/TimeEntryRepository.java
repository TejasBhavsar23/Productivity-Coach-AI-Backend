package com.productivitycoach.repository;

import com.productivitycoach.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TimeEntry entity.
 */
@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    /**
     * Find all time entries for a given DailyLog.
     */
    List<TimeEntry> findByDailyLogId(Long dailyLogId);

    /**
     * Find a TimeEntry by its ID, ensuring it belongs to the given DailyLog
     * (prevents manipulation of another user's entries).
     */
    Optional<TimeEntry> findByIdAndDailyLogId(Long id, Long dailyLogId);

    /**
     * Fetch all time entries for a user within a given set of DailyLog IDs.
     * Useful for aggregated weekly analytics.
     */
    @Query("SELECT te FROM TimeEntry te WHERE te.dailyLog.id IN :logIds ORDER BY te.startTime ASC")
    List<TimeEntry> findByDailyLogIds(@Param("logIds") List<Long> logIds);

    /**
     * Calculate the average productivity score across a user's logs.
     */
    @Query("SELECT AVG(te.productivityScore) FROM TimeEntry te WHERE te.dailyLog.id IN :logIds")
    Double averageProductivityScoreForLogs(@Param("logIds") List<Long> logIds);
}
