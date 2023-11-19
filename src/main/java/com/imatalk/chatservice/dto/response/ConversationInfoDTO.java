package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.ChatUser;
import com.imatalk.chatservice.entity.Conversation;
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
    private String status;




    public ConversationInfoDTO(Conversation conversation, ChatUser currentUser) {

        this.id = conversation.getId();

        if (conversation.isGroupConversation()) {
            this.name = conversation.getGroupName();
            this.avatar = conversation.getGroupAvatar();
        } else {
            // if the conversation is not a group conversation, then it is a direct conversation, and there are only 2 members in the conversation
            ChatUser otherUser = getTheOtherUserInConversation(currentUser, conversation);
            // for the current user, the conversation name and avatar are the other user's username and avatar,

            // if there is a nickname for the other user in this conversation, use the nickname otherwise use the display name
            String conversationName = conversation.getNicknameMap().getOrDefault(otherUser.getId(), otherUser.getDisplayName());

            this.name = conversationName;
            this.avatar = otherUser.getAvatar();
        }

        this.status = conversation.getStatus().toString();
        this.lastMessage = getLastMessageInConversation(conversation, currentUser);
        this.lastUpdate = getLastUpdateTimeOfConversation(conversation);
        this.unread =  checkIfConversationIsUnread(conversation, currentUser);
    }

    private LastMessageDTO getLastMessageInConversation(Conversation conversation, ChatUser currentUser) {

        if (conversation.getLastMessage() != null) {
            return new LastMessageDTO(conversation.getLastMessage());
        }

        // when the conversation has no message, create a default last message
        String lastMessageContent = null;
        if (conversation.isGroupConversation()) {
            lastMessageContent = "You has joined the conversation";
        } else {
            ChatUser otherUser = getTheOtherUserInConversation(currentUser, conversation);
            lastMessageContent = "Let's talk to " + otherUser.getDisplayName();
        }

        return new LastMessageDTO(Conversation.LastMessage.builder()
                .id("")
                .content(lastMessageContent)
                .messageNo(0L)
                .createdAt(conversation.getCreatedAt()) // TODO: if the conversation is a group conversation, this is the time when user join the conversation
                .build());
    }

    private LocalDateTime getLastUpdateTimeOfConversation(Conversation conversation) {
        // the last time the conversation was updated is the last time a message was sent,
        // or if there is no message, the time of the conversation creation

        if (conversation.getLastMessage() != null) {
            return conversation.getLastMessage().getCreatedAt();
        }

        return conversation.getCreatedAt();

    }


    @Data
    public static class LastMessageDTO {
        private String id;
        private String content;
        private LocalDateTime createdAt;

        public LastMessageDTO(Conversation.LastMessage message) {
            this.id = message.getId();
            this.content = message.getContent();
            this.createdAt = message.getCreatedAt();
        }

    }
    private boolean checkIfConversationIsUnread(Conversation conversation, ChatUser currentUser) {
        // if the current user has seen all messages in the conversation, then the conversation is seen
        Conversation.LastMessage lastMessage = conversation.getLastMessage();
        if (lastMessage == null) {
            return false ; // if there is no message, then the conversation is seen
        }

        long lastSeenMessageNo = conversation.getLastSeenMessageNoOfMember(currentUser.getId());

        // if the last seen message number of user is less than the message number of the latest message
        // it means that the user has not seen the latest message, so the conversation is unread
        return lastSeenMessageNo < lastMessage.getMessageNo();

    }



    public  ChatUser getTheOtherUserInConversation(ChatUser user, Conversation conversation) {

        // there can be only 2 members in a direct conversation
        // find the other user in the conversation
        ChatUser otherUser = conversation.getMembers().get(0);
        // if the first member is the current user, then the other user is the second member
        if (otherUser.getId().equals(user.getId())) {
            otherUser = conversation.getMembers().get(1);
        }
        return otherUser;
    }


}
