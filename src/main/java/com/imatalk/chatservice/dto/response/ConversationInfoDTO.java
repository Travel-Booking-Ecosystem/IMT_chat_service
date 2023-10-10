package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationInfoDTO {

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

    public ConversationInfoDTO(Conversation conversation, User currentUser) {
        // for the current user, the conversation name and avatar are the other user's username and avatar,
        // the last time the conversation was updated is the last time a message was sent,
        // or if there is no message, the time of the conversation creation
        Message lastMessage = conversation.getLastMessage();
        LocalDateTime lastUpdate = lastMessage != null ? lastMessage.getCreatedAt() : conversation.getCreatedAt();
        LastMessageDTO lastMessageDTO = convertToLastMessageDTO(lastMessage);


        this.id = conversation.getId();

        if (conversation.isGroupConversation()) {
            this.name = conversation.getGroupName();
            this.avatar = conversation.getGroupAvatar();
        } else {
            // if the conversation is not a group conversation, then it is a direct conversation, and there are only 2 members in the conversation
            User otherUser = getTheOtherUserInConversation(currentUser, conversation);
            this.name = otherUser.getDisplayName();
            this.avatar = otherUser.getAvatar();
        }

        this.lastMessage = lastMessageDTO;
        this.lastUpdate = lastUpdate;
        this.unread =  checkIfConversationIsUnread(conversation, currentUser);
    }

    private boolean checkIfConversationIsUnread(Conversation conversation, User currentUser) {
        // if the current user has seen all messages in the conversation, then the conversation is seen
        Message lastMessage = conversation.getLastMessage();
        if (lastMessage == null) {
            return false; // if there is no message, then the conversation is seen
        }

        long lastSeenMessageNo = conversation.getLastSeenMessageNoOfMember(currentUser.getId());

        // if the last seen message number of user is less than the message number of the latest message
        // it means that the user has not seen the latest message, so the conversation is unread
        return lastSeenMessageNo < lastMessage.getMessageNo();

    }


    private LastMessageDTO convertToLastMessageDTO(Message lastMessage) {
        if (lastMessage == null) {
            return null;
        }
        return new LastMessageDTO(lastMessage);
    }

    public  User getTheOtherUserInConversation(User user, Conversation conversation) {

        // there can be only 2 members in a direct conversation
        // find the other user in the conversation
        User otherUser = conversation.getMembers().get(0);
        // if the first member is the current user, then the other user is the second member
        if (otherUser.getId().equals(user.getId())) {
            otherUser = conversation.getMembers().get(1);
        }
        return otherUser;
    }


}
