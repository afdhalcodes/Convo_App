package com.project.convoback.repository;

import com.project.convoback.model.message.Conversation;
import com.project.convoback.model.message.ConversationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConversationRepo extends MongoRepository<Conversation, String> {

    // Check conversations initiated by one user towards another
    List<Conversation> findBySenderIdAndReceiverIdAndStatusIn(
            Long senderId,
            Long receiverId,
            List<ConversationStatus> statuses
    );

    List<Conversation> findByReceiverIdAndStatusOrderByCreatedAtDesc(
            Long receiverId,
            ConversationStatus status
    );

    List<Conversation> findBySenderIdOrReceiverIdOrderByUpdatedAtDesc(
            Long senderId,
            Long receiverId
    );
}
