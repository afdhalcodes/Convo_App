package com.project.convoback.service.converstation;

import com.project.convoback.dto.converstation.ConversationResponse;
import com.project.convoback.dto.converstation.CreateConversationRequest;
import com.project.convoback.model.message.Conversation;
import com.project.convoback.model.message.ConversationStatus;
import com.project.convoback.model.security.User;
import com.project.convoback.repository.ConversationRepo;
import com.project.convoback.repository.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Service
public class ConversationService {

    private final ConversationRepo conversationRepo;
    private final UserRepo userRepo;

    public ConversationService(
            ConversationRepo conversationRepo,
            UserRepo userRepo
    ) {
        this.conversationRepo = conversationRepo;
        this.userRepo = userRepo;
    }

    private User getCurrentUser(Authentication authentication) {

        String username = authentication.getName();

        return userRepo.findByEmailOrUsername(username, username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Authenticated user not found"
                        )
                );
    }


    public ConversationResponse createConversation(
            CreateConversationRequest request,
            Authentication authentication
    ) {

        User sender = getCurrentUser(authentication);

        User receiver = userRepo.findByEmailOrUsername(
                        request.getReceiverUsername(),
                        request.getReceiverUsername()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Receiver not found"
                        )
                );

        if (sender.getId().equals(receiver.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot start a conversation with yourself"
            );
        }

        // Check whether the receiver has already sent
        // an invitation to the current user.
        Optional<Conversation> existingInvitation =
                conversationRepo.findBySenderIdAndReceiverIdAndStatusIn(
                        receiver.getId(),
                        sender.getId(),
                        List.of(ConversationStatus.PENDING)
                ).stream().findFirst();

        if (existingInvitation.isPresent()) {

            Conversation conversation = existingInvitation.get();

            conversation.setStatus(ConversationStatus.ACCEPTED);
            conversation.setUpdatedAt(Instant.now());

            Conversation saved = conversationRepo.save(conversation);

            return convertToResponse(saved);
        }

        // Check whether the current user has already
        // sent an invitation to the receiver.
        Optional<Conversation> existingRequest =
                conversationRepo.findBySenderIdAndReceiverIdAndStatusIn(
                        sender.getId(),
                        receiver.getId(),
                        List.of(
                                ConversationStatus.PENDING,
                                ConversationStatus.ACCEPTED
                        )
                ).stream().findFirst();

        if (existingRequest.isPresent()) {
            return convertToResponse(existingRequest.get());
        }

        // No existing invitation: create a new request.
        Conversation conversation = new Conversation(
                sender.getId(),
                receiver.getId()
        );

        Conversation saved = conversationRepo.save(conversation);

        return convertToResponse(saved);
    }


    private void deletePendingDuplicates(
            List<Conversation> senderInvites,
            List<Conversation> receiverInvites,
            String keepConversationId
    ) {

        List<Conversation> allInvites = new java.util.ArrayList<>();
        allInvites.addAll(senderInvites);
        allInvites.addAll(receiverInvites);

        for (Conversation invite : allInvites) {

            boolean isPending =
                    invite.getStatus() == ConversationStatus.PENDING;

            boolean isNotTheConversationToKeep =
                    !invite.getId().equals(keepConversationId);

            if (isPending && isNotTheConversationToKeep) {
                conversationRepo.delete(invite);
            }
        }
    }


    public List<ConversationResponse> getMyConversations(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return conversationRepo
                .findBySenderIdOrReceiverIdOrderByUpdatedAtDesc(
                        user.getId(),
                        user.getId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<ConversationResponse> getIncomingRequests(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return conversationRepo
                .findByReceiverIdAndStatusOrderByCreatedAtDesc(
                        user.getId(),
                        ConversationStatus.PENDING
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public ConversationResponse acceptConversation(
            String conversationId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Conversation conversation = getConversation(conversationId);

        if (!conversation.getReceiverId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot accept this request"
            );
        }

        if (conversation.getStatus() != ConversationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request is no longer pending"
            );
        }

        conversation.setStatus(ConversationStatus.ACCEPTED);
        conversation.setUpdatedAt(Instant.now());

        Conversation saved =
                conversationRepo.save(conversation);

        return convertToResponse(saved);
    }

    public ConversationResponse rejectConversation(
            String conversationId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        Conversation conversation = getConversation(conversationId);

        if (!conversation.getReceiverId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot reject this request"
            );
        }

        if (conversation.getStatus() != ConversationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request is no longer pending"
            );
        }

        conversation.setStatus(ConversationStatus.REJECTED);
        conversation.setUpdatedAt(Instant.now());

        Conversation saved =
                conversationRepo.save(conversation);

        return convertToResponse(saved);
    }

    private ConversationResponse convertToResponse(
            Conversation conversation
    ) {

        User sender = userRepo.findById(conversation.getSenderId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Conversation sender not found"
                        )
                );

        User receiver = userRepo.findById(conversation.getReceiverId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Conversation receiver not found"
                        )
                );

        return new ConversationResponse(
                conversation.getId(),

                sender.getId(),
                sender.getUsername(),
                sender.getEmail(),

                receiver.getId(),
                receiver.getUsername(),
                receiver.getEmail(),

                conversation.getStatus(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    // Find a conversation by ID
    private Conversation getConversation(String conversationId) {

        return conversationRepo.findById(conversationId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Conversation not found"
                        )
                );
    }
}
