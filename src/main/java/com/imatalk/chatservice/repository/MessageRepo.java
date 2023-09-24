package com.imatalk.chatservice.repository;

import com.imatalk.chatservice.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends MongoRepository<Message, String> {
    List<Message> findAllByConversationIdAndMessageNoGreaterThanEqualOrderByCreatedAt(String conversationId, long messageNo);
}
