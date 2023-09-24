package com.imatalk.chatservice.repository;

import com.imatalk.chatservice.entity.DirectConversation;
import com.imatalk.chatservice.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DirectConversationRepo extends MongoRepository<DirectConversation, String> {
    Optional<DirectConversation> findByMembersIn(List<User> currentUser);

}
