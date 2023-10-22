package com.imatalk.chatservice.repository;

import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConversationRepo extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByMembersIn(List<User> currentUser);
    List<Conversation> findAllByIdInOrderByLastUpdatedAtDesc(List<String> conversationIds);
}
