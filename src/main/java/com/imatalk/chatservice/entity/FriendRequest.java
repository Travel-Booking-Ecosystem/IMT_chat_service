package com.imatalk.chatservice.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "friend_requests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FriendRequest {
    private String id;
    private User sender;
    private User receiver;
    private LocalDateTime createdAt;
    private boolean isAccepted;
}
