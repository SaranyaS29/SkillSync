package com.freelanceplatform.repository;
import com.freelanceplatform.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ConversationRepository extends JpaRepository<Conversation, Long> { Optional<Conversation> findFirstByProjectId(Long projectId); }
