package com.project.convoback.repository;

import com.project.convoback.model.message.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepo extends MongoRepository<Message, String> {

    List<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(String conversationId);

}
