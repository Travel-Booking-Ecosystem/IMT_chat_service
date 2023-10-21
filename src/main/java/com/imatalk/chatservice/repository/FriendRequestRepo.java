package com.imatalk.chatservice.repository;

import com.imatalk.chatservice.entity.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepo extends MongoRepository<FriendRequest, String> {
}
