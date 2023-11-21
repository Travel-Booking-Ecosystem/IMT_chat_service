package com.imatalk.chatservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewMessageEvent {
    private List<String> conversationMemberIds;
    private Message message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String id;
        private String senderId;
        private String content;
        private String createdAt;
        private String conversationId;
        private String messageType;
        private long messageNo;
        private RepliedMessage repliedMessage; // this is the id of the message that this message is replying to
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RepliedMessage {
        private String id;
        private String messageContent;
        private String senderName;

    }
}
