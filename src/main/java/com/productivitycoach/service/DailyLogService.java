package com.productivitycoach.service;

import com.productivitycoach.dto.request.DailyLogRequest;
import com.productivitycoach.dto.request.TimeEntryRequest;
import com.productivitycoach.dto.response.DailyLogResponse;
import com.productivitycoach.dto.response.TimeEntryResponse;
import com.productivitycoach.entity.DailyLog;
import com.productivitycoach.entity.TimeEntry;
import com.productivitycoach.entity.User;
import com.productivitycoach.exception.BadRequestException;
import com.productivitycoach.exception.DuplicateResourceException;
import com.productivitycoach.exception.ResourceNotFoundException;
import com.productivitycoach.repository.DailyLogRepository;
import com.productivitycoach.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for DailyLog and TimeEntry management.
 *
 * A DailyLog is the container for a day's worth of time entries.
 * Only one DailyLog per user per date is allowed (enforced at DB and service level).
 */
@Service
public class DailyLogService {

    private static final Logger logger = LoggerFactory.getLogger(DailyLogService.class);

    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;

    public DailyLogService(DailyLogRepository dailyLogRepository,
                           UserRepository userRepository) {
        this.dailyLogRepository = dailyLogRepository;
        this.userRepository = userRepository;
    }

    // ----------------------------------------------------------------
    // CREATE LOG
    // ----------------------------------------------------------------

    @Transactional
    public DailyLogResponse createLog(DailyLogRequest request, Long userId) {
        logger.info("Creating DailyLog for userId={}, date={}", userId, request.getDate());

        User user = findUser(userId);

        // Enforce: one log per user per day
        if (dailyLogRepository.findByUserIdAndDate(userId, request.getDate()).isPresent()) {
            throw new DuplicateResourceException(
                    "A log already exists for date: " + request.getDate() +
                    ". Use PUT /logs/{id} to update it.");
        }

        DailyLog log = new DailyLog(request.getDate(), user);

        // Add any time entries provided at creation time
        if (request.getTimeEntries() != null) {
            for (TimeEntryRequest teReq : request.getTimeEntries()) {
                validateTimeEntry(teReq);
                TimeEntry entry = mapToTimeEntry(teReq, log);
                log.getTimeEntries().add(entry);
            }
        }

        DailyLog saved = dailyLogRepository.save(log);
        logger.info("DailyLog created: id={}, date={}, userId={}", saved.getId(), saved.getDate(), userId);

        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // GET BY DATE
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public DailyLogResponse getLogByDate(LocalDate date, Long userId) {
        DailyLog log = dailyLogRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new ResourceNotFoundException("DailyLog", "date", date));
        return toResponse(log);
    }

    // ----------------------------------------------------------------
    // UPDATE LOG (add / replace time entries)
    // ----------------------------------------------------------------

    @Transactional
    public DailyLogResponse updateLog(Long logId, DailyLogRequest request, Long userId) {
        logger.info("Updating DailyLog id={} for userId={}", logId, userId);

        DailyLog log = dailyLogRepository.findByIdAndUserId(logId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyLog", "id", logId));

        // Update date if changed (check for conflicts with other logs on that date)
        if (!log.getDate().equals(request.getDate())) {
            dailyLogRepository.findByUserIdAndDate(userId, request.getDate()).ifPresent(existing -> {
                if (!existing.getId().equals(logId)) {
                    throw new DuplicateResourceException(
                            "Another log already exists for date: " + request.getDate());
                }
            });
            log.setDate(request.getDate());
        }

        // Replace all time entries
        log.getTimeEntries().clear();
        if (request.getTimeEntries() != null) {
            for (TimeEntryRequest teReq : request.getTimeEntries()) {
                validateTimeEntry(teReq);
                log.getTimeEntries().add(mapToTimeEntry(teReq, log));
            }
        }

        DailyLog updated = dailyLogRepository.save(log);
        logger.info("DailyLog updated: id={}", updated.getId());

        return toResponse(updated);
    }

    // ----------------------------------------------------------------
    // DELETE LOG
    // ----------------------------------------------------------------

    @Transactional
    public void deleteLog(Long logId, Long userId) {
        logger.info("Deleting DailyLog id={} for userId={}", logId, userId);

        DailyLog log = dailyLogRepository.findByIdAndUserId(logId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("DailyLog", "id", logId));

        dailyLogRepository.delete(log);
        logger.info("DailyLog deleted: id={}", logId);
    }

    // ----------------------------------------------------------------
    // INTERNAL HELPERS
    // ----------------------------------------------------------------

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * Business validation: endTime must be after startTime.
     */
    private void validateTimeEntry(TimeEntryRequest req) {
        if (req.getEndTime().isBefore(req.getStartTime()) ||
            req.getEndTime().equals(req.getStartTime())) {
            throw new BadRequestException(
                    "endTime must be after startTime for activity: " + req.getActivity());
        }
    }

    private TimeEntry mapToTimeEntry(TimeEntryRequest req, DailyLog log) {
        return new TimeEntry(
                req.getStartTime(),
                req.getEndTime(),
                req.getActivity(),
                req.getCategory(),
                req.getProductivityScore(),
                log
        );
    }

    /**
     * Maps a DailyLog entity (with its TimeEntries) to a response DTO.
     */
    public DailyLogResponse toResponse(DailyLog log) {
        List<TimeEntryResponse> entryResponses = log.getTimeEntries()
                .stream()
                .map(te -> new TimeEntryResponse(
                        te.getId(),
                        te.getStartTime(),
                        te.getEndTime(),
                        te.getActivity(),
                        te.getCategory(),
                        te.getProductivityScore(),
                        te.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new DailyLogResponse(
                log.getId(),
                log.getDate(),
                entryResponses,
                log.getCreatedAt(),
                log.getUpdatedAt()
        );
    }
}
