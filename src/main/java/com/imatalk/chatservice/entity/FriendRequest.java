package com.imatalk.chatservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document(collection = "friend_requests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString(exclude = {"sender", "receiver"})
public class FriendRequest {

    @Id
    @MongoId // what is the difference between @Id and @MongoId?
    private String id;
    @DBRef
    private User sender;
    @DBRef
    private User receiver;
    private LocalDateTime createdAt;
    private boolean isAccepted;
}
