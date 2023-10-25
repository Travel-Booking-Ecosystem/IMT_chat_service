package com.imatalk.chatservice.mongoRepository;

import com.imatalk.chatservice.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findAllByUserIdAndUnread(String userId, boolean unread);
}
