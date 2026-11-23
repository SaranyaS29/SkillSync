package com.freelanceplatform.service;

import com.freelanceplatform.dto.message.*;
import com.freelanceplatform.entity.*;
import com.freelanceplatform.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class ConversationService {
    private final ConversationRepository conversations; private final MessageRepository messages;
    private final ProjectRepository projects; private final AssignmentRepository assignments; private final UserAccountRepository users;
    private final NotificationService notificationService;
    public ConversationService(ConversationRepository conversations, MessageRepository messages, ProjectRepository projects,
            AssignmentRepository assignments, UserAccountRepository users, NotificationService notificationService) {
        this.conversations=conversations; this.messages=messages; this.projects=projects; this.assignments=assignments; this.users=users; this.notificationService=notificationService;
    }
    @Transactional
    public Long openForProject(Long projectId, String email) {
        Project project = projects.findByIdForUpdate(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));
        Assignment assignment = assignments.findByProjectId(projectId).orElseThrow(() -> new IllegalArgumentException("Conversation is available after assignment"));
        UserAccount user=users.findByEmailIgnoreCase(email).orElseThrow(); ensureParticipant(assignment,user);
        return conversations.findFirstByProjectId(projectId).orElseGet(() -> {
            Conversation conversation=new Conversation(); conversation.setProject(project); conversation.getParticipants().add(assignment.getClient()); conversation.getParticipants().add(assignment.getFreelancer()); return conversations.save(conversation);
        }).getId();
    }
    @Transactional(readOnly=true)
    public Page<MessageResponse> list(Long conversationId, String email, Pageable pageable) {
        Conversation c=conversations.findById(conversationId).orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        ensureConversationMember(c,email);
        return messages.findByConversationIdOrderBySentAtDesc(conversationId,pageable).map(this::response);
    }
    @Transactional
    public MessageResponse send(Long conversationId, String email, MessageRequest request) {
        Conversation c=conversations.findById(conversationId).orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        ensureConversationMember(c,email);
        UserAccount sender=users.findByEmailIgnoreCase(email).orElseThrow();
        Message m=new Message(); m.setConversation(c); m.setSender(sender); m.setBody(request.body().trim()); m=messages.save(m);
        c.getParticipants().stream().filter(p -> !p.getId().equals(sender.getId())).forEach(recipient -> notificationService.notify(recipient,"NEW_MESSAGE","CONVERSATION",c.getId(),"New message on project: "+c.getProject().getTitle()));
        return response(m);
    }
    @Transactional
    public void markRead(Long conversationId, String email) {
        Conversation c=conversations.findById(conversationId).orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        ensureConversationMember(c,email); UserAccount reader=users.findByEmailIgnoreCase(email).orElseThrow();
        messages.markConversationRead(conversationId,reader.getId(),Instant.now());
    }
    private void ensureConversationMember(Conversation c,String email) {
        if (c.getParticipants().stream().noneMatch(p -> p.getEmail().equalsIgnoreCase(email))) throw new IllegalArgumentException("Conversation not found");
    }
    private void ensureParticipant(Assignment a,UserAccount u) {
        if (!a.getClient().getId().equals(u.getId()) && !a.getFreelancer().getId().equals(u.getId())) throw new IllegalArgumentException("Project not found");
    }
    private MessageResponse response(Message m) { return new MessageResponse(m.getId(),m.getConversation().getId(),m.getSender().getId(),m.getSender().getFullName(),m.getBody(),m.getSentAt(),m.getReadAt()); }
}
