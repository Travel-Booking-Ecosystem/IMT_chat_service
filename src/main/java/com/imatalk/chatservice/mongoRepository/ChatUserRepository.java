package com.imatalk.chatservice.mongoRepository;

import com.imatalk.chatservice.entity.ChatUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatUserRepository extends MongoRepository<ChatUser, String> {
}
