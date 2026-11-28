package com.project.convoback.dto.converstation;

import com.project.convoback.model.message.ConversationStatus;

import java.time.Instant;

public class ConversationResponse {

    private String id;

    private Long senderId;

    private String senderUsername;

    private String senderEmail;

    private Long receiverId;

    private String receiverUsername;

    private String receiverEmail;

    private ConversationStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    public ConversationResponse(
            String id,
            Long senderId,
            String senderUsername,
            String senderEmail,
            Long receiverId,
            String receiverUsername,
            String receiverEmail,
            ConversationStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;

        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.senderEmail = senderEmail;

        this.receiverId = receiverId;
        this.receiverUsername = receiverUsername;
        this.receiverEmail = receiverEmail;

        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }
}
