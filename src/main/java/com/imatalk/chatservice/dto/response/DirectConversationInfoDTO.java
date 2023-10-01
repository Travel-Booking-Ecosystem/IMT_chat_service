package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.DirectConversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DirectConversationInfoDTO {

    // this object only contains the information of the conversation, not the messages
    private String id;
    private String name;
    private String avatar;
    private LastMessageDTO lastMessage;
    private LocalDateTime lastUpdate; // this is the time of the last message or if there is no message, the time of the conversation creation
    private boolean unread;

    @Data
    public static class LastMessageDTO {
        private String id;
        private String content;
        private LocalDateTime createdAt;

        public LastMessageDTO(Message message) {
            this.id = message.getId();
            this.content = message.getContent();
            this.createdAt = message.getCreatedAt();
        }

    }

    public DirectConversationInfoDTO(DirectConversation conversation, User otherUser, Message lastMessage) {
        // for the current user, the conversation name and avatar are the other user's username and avatar,
        // the last time the conversation was updated is the last time a message was sent,
        // or if there is no message, the time of the conversation creation
        LocalDateTime lastUpdate = lastMessage != null ? lastMessage.getCreatedAt() : conversation.getCreatedAt();
        LastMessageDTO lastMessageDTO = lastMessage != null ? new LastMessageDTO(lastMessage) : null;

        this.id = conversation.getId();
        this.name = otherUser.getDisplayName();
        this.avatar = otherUser.getAvatar();
        this.lastMessage = lastMessageDTO;
        this.lastUpdate = lastUpdate;
    }

}
