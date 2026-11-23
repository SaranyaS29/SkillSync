package com.freelanceplatform.dto.report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ReportRequest(Long targetUserId,Long projectId,@NotBlank @Size(max=4000) String reason) {}
