package com.imatalk.chatservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdatedEvent {
    private String userId;
    // because the chat-service only needs these 2 fields
    private String displayName;
    private String avatar;
}
