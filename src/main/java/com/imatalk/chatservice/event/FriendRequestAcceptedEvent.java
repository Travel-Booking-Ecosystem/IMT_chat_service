package com.imatalk.chatservice.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestAcceptedEvent {
    //TODO: implement this class
    private String requestId;
    private String senderId;
    private LocalDateTime acceptedAt;
    private Receiver receiver;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Receiver {
        private String id;
        private String username;
        private String displayName;
        private String avatar;
        private String email;
    }
}
