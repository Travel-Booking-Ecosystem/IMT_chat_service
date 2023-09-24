package com.imatalk.chatservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

// this object contains the list of members and the list of messages of the conversation
@Data
public class DirectConversationMessagesDTO {
    private String conversationId;
    // using map for easier access for the frontend
    private Map<String, MemberDTO> members; // key as the user id, and value as the member object
    private Map<String, DirectMessageDTO> messages;

    @Data
    public static class MemberDTO {
        private String id;
        private String displayName;
        private String username;
        private String avatar;
        private LastSeen lastSeen; // this is the last message seen by the user in the conversation

        public MemberDTO (User user) {
            this.id = user.getId();
            this.displayName = user.getDisplayName();
            this.username = user.getUsername();
            this.avatar = user.getAvatar();
        }


        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class LastSeen {
//            private String messageId;
            private long messageNo;
        }
    }

    @Data
    public static class DirectMessageDTO {
        private String id;
        private String senderId;
        private String content;
//        private String type;
        private boolean senderIsMe;
        private String createdAt;
        private long messageNo;

        private String repliedMessageId; // this is the id of the message that this message is replying to

        public DirectMessageDTO(Message message, User currentUser) {
            this.id = message.getId();
            this.senderId = message.getSenderId();
            this.content = message.getContent();
            this.senderIsMe = message.getSenderId().equals(currentUser.getId());
            this.createdAt = message.getCreatedAt().toString();
            this.messageNo = message.getMessageNo();
            this.repliedMessageId = message.getRepliedMessageId();
        }
    }


}
