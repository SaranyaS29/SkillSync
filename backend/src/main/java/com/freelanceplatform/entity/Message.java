package com.freelanceplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="messages", indexes=@Index(name="idx_message_conversation_sent", columnList="conversation_id,sent_at"))
@Getter @Setter @NoArgsConstructor
public class Message {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="conversation_id", nullable=false) private Conversation conversation;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="sender_id", nullable=false) private UserAccount sender;
    @Column(nullable=false, columnDefinition="text") private String body;
    @Column(name="sent_at", nullable=false, updatable=false) private Instant sentAt;
    @Column(name="read_at") private Instant readAt;
    @PrePersist void onCreate() { sentAt = Instant.now(); }
}
