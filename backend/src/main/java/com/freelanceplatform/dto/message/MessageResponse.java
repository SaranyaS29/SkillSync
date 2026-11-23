package com.freelanceplatform.dto.message;
import java.time.Instant;
public record MessageResponse(Long id, Long conversationId, Long senderId, String senderName, String body, Instant sentAt, Instant readAt) {}
