package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import com.imatalk.chatservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// this object contains the list of members and the list of messages of the conversation
@Data
public class ConversationChatHistoryDTO {
    private String conversationId;
    private String conversationName;
    private String conversationAvatar;
    private Map<String, MemberDTO> memberMap; // map of members for fast retrieval, key is the member id, value is the member
    // using map for easier access for the frontend
    private Map<String, MessageDTO> messageMap; // map of messages for fast retrieval, key is the message id, value is the message
    private List<MessageDTO> messageList; // list of messages in the conversation

    public ConversationChatHistoryDTO(Conversation conversation, User currentUser, List<Message> messages) {
        this.conversationId = conversation.getId();

        if (conversation.isGroupConversation()) {
            this.conversationName = conversation.getGroupName();
            this.conversationAvatar = conversation.getGroupAvatar();
        } else {
            // if the conversation is not a group conversation, then it is a direct conversation, and there are only 2 members in the conversation
            User otherUser =  getTheOtherUserInConversation(currentUser, conversation);
            this.conversationName = otherUser.getDisplayName();
            this.conversationAvatar = otherUser.getAvatar();
        }

        this.memberMap = convertMemberListToMap(conversation);
        this.messageMap = convertMessageListToMap(messages);
        this.messageList = List.copyOf(messageMap.values());

    }
    private Map<String, MemberDTO> convertMemberListToMap(Conversation conversation) {
        Map<String, MemberDTO> map = new HashMap<>();
        for (User member : conversation.getMembers()) {
            MemberDTO otherUser = new MemberDTO(member);
            MemberDTO.LastSeen lastSeen = new MemberDTO.LastSeen(conversation.getLastSeenMessageNoOfMember(member.getId()));
            otherUser.setLastSeen(lastSeen);
            map.put(member.getId(), otherUser);
        }


        return map;
    }

    private Map<String, MessageDTO> convertMessageListToMap(List<Message> messages) {
        Map<String, MessageDTO> map = new LinkedHashMap<>(); // using LinkedHashMap to preserve the order of insertion (the order of messages)
        for (Message message : messages) {
            MessageDTO dto = new MessageDTO(message);
            map.put(dto.getId(), dto);
        }
        return map;
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberDTO {
        private String id;
        private String displayName;
        private String username;
        private String avatar;
        private LastSeen lastSeen; // this is the last message seen by the user in the conversation

        public MemberDTO(User user) {
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
    public static class MessageDTO {
        private String id;
        private String senderId;
        private String content;
//        private String type;
        private String createdAt;
        private long messageNo;

        private String repliedMessageId; // this is the id of the message that this message is replying to

        public MessageDTO(Message message) {
            this.id = message.getId();
            this.senderId = message.getSenderId();
            this.content = message.getContent();
            this.createdAt = message.getCreatedAt().toString();
            this.messageNo = message.getMessageNo();
            this.repliedMessageId = message.getRepliedMessageId();
        }
    }



}
