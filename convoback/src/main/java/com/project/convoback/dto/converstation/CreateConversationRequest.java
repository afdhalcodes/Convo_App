package com.project.convoback.dto.converstation;

import jakarta.validation.constraints.NotBlank;

public class CreateConversationRequest{

    @NotBlank(message = "Receiver username is required")
    private String receiverUsername;

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }
}
