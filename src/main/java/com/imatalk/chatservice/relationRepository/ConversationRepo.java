package com.imatalk.chatservice.relationRepository;

import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConversationRepo extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByMembersIn(List<User> currentUser);
    List<Conversation> findAllByIdInOrderByLastUpdatedAtDesc(List<String> conversationIds);
}
