package com.imatalk.chatservice.mongoRepository;

import com.imatalk.chatservice.entity.ChatUser;
import com.imatalk.chatservice.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    // find all direct conversation of a user
    List<Conversation> findAllByMembersInAndIsGroupConversation(List<ChatUser> members, boolean isGroupConversation);

    List<Conversation> findAllByMembers_IdInOrderByLastUpdatedAtDesc(List<String> members);

    List<Conversation> findAllByMembersInOrderByLastUpdatedAtDesc(List<ChatUser> members);
}
