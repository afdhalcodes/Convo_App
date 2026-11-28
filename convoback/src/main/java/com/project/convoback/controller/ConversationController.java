package com.project.convoback.controller;

import com.project.convoback.dto.converstation.ConversationResponse;
import com.project.convoback.dto.converstation.CreateConversationRequest;
import com.project.convoback.service.converstation.ConversationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService
    ) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ConversationResponse createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            Authentication authentication
    ) {
        return conversationService.createConversation(
                request,
                authentication
        );
    }

    @GetMapping
    public List<ConversationResponse> getMyConversations(
            Authentication authentication
    ) {
        return conversationService.getMyConversations(
                authentication
        );
    }

    @GetMapping("/requests")
    public List<ConversationResponse> getIncomingRequests(
            Authentication authentication
    ) {
        return conversationService.getIncomingRequests(
                authentication
        );
    }

    @PatchMapping("/{id}/accept")
    public ConversationResponse acceptConversation(
            @PathVariable String id,
            Authentication authentication
    ) {
        return conversationService.acceptConversation(
                id,
                authentication
        );
    }

    @PatchMapping("/{id}/reject")
    public ConversationResponse rejectConversation(
            @PathVariable String id,
            Authentication authentication
    ) {
        return conversationService.rejectConversation(
                id,
                authentication
        );
    }
}
