package com.freelanceplatform.dto.milestone;

import com.freelanceplatform.entity.Milestone;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MilestoneResponse(Long id, Long assignmentId, String title, String description, LocalDate dueDate,
        BigDecimal amount, Integer sequenceNumber, Milestone.MilestoneStatus status, List<SubmissionSummary> submissions,
        List<ProgressUpdate> progressUpdates) {
    public record SubmissionSummary(Long id, String description, String comments, String submittedBy, java.time.Instant submittedAt, java.util.List<FileSummary> files) {}
    public record FileSummary(Long id,String originalName,String downloadUrl) {}
    public record ProgressUpdate(String fromStatus, String toStatus, String comment, String changedBy, java.time.Instant changedAt) {}
}
