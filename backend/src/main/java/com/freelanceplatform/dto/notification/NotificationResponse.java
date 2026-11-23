package com.freelanceplatform.dto.notification;
import java.time.Instant;
public record NotificationResponse(Long id, String type, String resourceType, Long resourceId, String message, Instant readAt, Instant createdAt) {}
