package com.freelanceplatform.dto.report;
import java.time.Instant;
public record ReportResponse(Long id,Long reporterId,String reporterName,Long targetUserId,String targetUserName,Long projectId,String projectTitle,String status,String reason,Instant createdAt) {}
