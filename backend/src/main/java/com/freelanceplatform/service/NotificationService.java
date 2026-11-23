package com.freelanceplatform.service;

import com.freelanceplatform.dto.notification.NotificationResponse;
import com.freelanceplatform.entity.Notification;
import com.freelanceplatform.entity.UserAccount;
import com.freelanceplatform.repository.NotificationRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class NotificationService {
    private final NotificationRepository notifications; private final UserAccountRepository users;
    public NotificationService(NotificationRepository notifications, UserAccountRepository users) { this.notifications=notifications; this.users=users; }
    @Transactional
    public void notify(UserAccount user, String type, String resourceType, Long resourceId, String message) {
        Notification n = new Notification(); n.setUser(user); n.setType(type); n.setResourceType(resourceType); n.setResourceId(resourceId); n.setMessage(message); notifications.save(n);
    }
    @Transactional(readOnly=true)
    public Page<NotificationResponse> list(String email, Pageable pageable) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        return notifications.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable).map(this::response);
    }
    @Transactional(readOnly=true)
    public long unreadCount(String email) { return notifications.countByUserIdAndReadAtIsNull(users.findByEmailIgnoreCase(email).orElseThrow().getId()); }
    @Transactional
    public NotificationResponse markRead(Long id, String email) {
        var n = notifications.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getUser().getEmail().equalsIgnoreCase(email)) throw new IllegalArgumentException("Notification not found");
        if (n.getReadAt() == null) n.setReadAt(Instant.now());
        return response(n);
    }
    private NotificationResponse response(Notification n) { return new NotificationResponse(n.getId(), n.getType(), n.getResourceType(), n.getResourceId(), n.getMessage(), n.getReadAt(), n.getCreatedAt()); }
}
