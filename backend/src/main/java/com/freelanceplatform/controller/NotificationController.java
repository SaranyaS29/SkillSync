package com.freelanceplatform.controller;

import com.freelanceplatform.dto.notification.NotificationResponse;
import com.freelanceplatform.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service=service; }
    @GetMapping @Operation(summary="List your notifications")
    public Page<NotificationResponse> list(Authentication auth, @PageableDefault(size=20) Pageable pageable) { return service.list(auth.getName(), pageable); }
    @GetMapping("/unread-count") public long unreadCount(Authentication auth) { return service.unreadCount(auth.getName()); }
    @PutMapping("/{id}/read") @Operation(summary="Mark one of your notifications read")
    public NotificationResponse markRead(@PathVariable Long id, Authentication auth) { return service.markRead(id, auth.getName()); }
}
