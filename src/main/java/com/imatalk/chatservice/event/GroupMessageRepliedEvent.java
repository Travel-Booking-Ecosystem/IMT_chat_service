package com.imatalk.chatservice.event;

import com.imatalk.chatservice.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMessageRepliedEvent {
    //TODO: this is still missing

    private RepliedMessage message; // the message that is being replied to
    private Replier replier;
    private Conversation conversation;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Conversation {
        // this object only contains the information of the conversation, not the messages
        private String id;
        private String name;
        private String avatar;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RepliedMessage {
        private String id;
        private String senderId;
        private String content;
        private LocalDateTime createdAt;
        private String conversationId;
        private long messageNo;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Replier {
        private String id;
        private String username;
        private String displayName;
        private String avatar;
    }

}
