package com.freelanceplatform.controller;

import com.freelanceplatform.dto.message.*;
import com.freelanceplatform.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
public class ConversationController {
    private final ConversationService service;
    public ConversationController(ConversationService service) { this.service=service; }
    @PostMapping("/projects/{projectId}/conversation") @ResponseStatus(HttpStatus.CREATED) @Operation(summary="Open the assigned project's conversation")
    public java.util.Map<String,Long> open(@PathVariable Long projectId, Authentication auth) { return java.util.Map.of("conversationId",service.openForProject(projectId,auth.getName())); }
    @GetMapping("/conversations/{id}/messages") public Page<MessageResponse> messages(@PathVariable Long id,Authentication auth,@PageableDefault(size=50) Pageable pageable) { return service.list(id,auth.getName(),pageable); }
    @PostMapping("/conversations/{id}/messages") public MessageResponse send(@PathVariable Long id,@Valid @RequestBody MessageRequest request,Authentication auth) { return service.send(id,auth.getName(),request); }
    @PutMapping("/conversations/{id}/read") public void markRead(@PathVariable Long id,Authentication auth) { service.markRead(id,auth.getName()); }
}
