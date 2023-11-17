package com.imatalk.chatservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("chat_users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChatUser {
    // This class is used to store user information in the conversation document
    @Id
    private String id;
    private String displayName;
    private String username; // this is unique, like @john_due21
    private String avatar;
    private String currentConversationId; // the id of the conversation that the user is currently in
}
