package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);
    @Modifying
    @Query("update Message m set m.readAt = :readAt where m.conversation.id = :conversationId and m.sender.id <> :readerId and m.readAt is null")
    int markConversationRead(@Param("conversationId") Long conversationId, @Param("readerId") Long readerId, @Param("readAt") Instant readAt);
}
