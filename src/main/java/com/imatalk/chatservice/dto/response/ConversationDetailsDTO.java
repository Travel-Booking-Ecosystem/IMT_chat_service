package com.imatalk.chatservice.dto.response;

import com.imatalk.chatservice.entity.ChatUser;
import com.imatalk.chatservice.entity.Conversation;
import com.imatalk.chatservice.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this object contains the list of members and the list of messages of the conversation
@Data
public class ConversationDetailsDTO {
    private String conversationId;
    private String conversationName;
    private String conversationAvatar;
    private ConversationSettingDTO conversationSetting;
    private boolean isGroupConversation;

    private Map<String, MemberDTO> memberMap; // map of members for fast retrieval, key is the member id, value is the member
    // using map for easier access for the frontend
//    private Map<String, MessageDTO> messageMap; // map of messages for fast retrieval, key is the message id, value is the message
    private List<MessageDTO> messageList; // list of messages in the conversation
    @Data
    public static class ConversationSettingDTO {
        private String themeColor;
        private String wallpaper;
        private String defaultReaction;

        public ConversationSettingDTO(Conversation.ConversationSetting conversationSetting) {
            String defaultThemColor = "CHAT-COLOR-1";
            String defaultWallpaper = "NO-WALLPAPER";
            String defaultEmoji = "LIKE";

            if (conversationSetting != null) {
                this.themeColor = conversationSetting.getThemeColor() != null ? conversationSetting.getThemeColor() : defaultThemColor;
                this.wallpaper = conversationSetting.getWallpaper() != null ? conversationSetting.getWallpaper() : defaultWallpaper;
                this.defaultReaction = conversationSetting.getDefaultReaction() != null ? conversationSetting.getDefaultReaction().toString() : defaultEmoji;
            } else {
                this.themeColor = defaultThemColor;
                this.wallpaper = defaultWallpaper;
                this.defaultReaction = defaultEmoji;
            }
        }
    }


    //TODO: move this data to service layer
    public ConversationDetailsDTO(Conversation conversation, ChatUser currentUser, List<Message> messages) {
        this.conversationId = conversation.getId();

        if (conversation.isGroupConversation()) {
            this.conversationName = conversation.getGroupName();
            this.conversationAvatar = conversation.getGroupAvatar();
        } else {
            // if the conversation is not a group conversation, then it is a direct conversation, and there are only 2 members in the conversation
            ChatUser otherUser =  getTheOtherUserInConversation(currentUser, conversation);
            this.conversationName = otherUser.getDisplayName();
            this.conversationAvatar = otherUser.getAvatar();
        }
        this.isGroupConversation = conversation.isGroupConversation();

        // the memberMap needs to be constructed before the messageList because the messageList may use the memberMap
        this.memberMap = convertMemberListToMap(conversation);
        this.messageList = convertMessageListToMap(messages);
        this.conversationSetting = new ConversationSettingDTO(conversation.getConversationSetting());

    }
    private Map<String, MemberDTO> convertMemberListToMap(Conversation conversation) {
        Map<String, MemberDTO> map = new HashMap<>();
        for (ChatUser member : conversation.getMembers()) {
            MemberDTO otherUser = new MemberDTO(member);
            MemberDTO.LastSeen lastSeen = new MemberDTO.LastSeen(conversation.getLastSeenMessageNoOfMember(member.getId()));
            otherUser.setLastSeen(lastSeen);
            map.put(member.getId(), otherUser);
        }


        return map;
    }

    private List<MessageDTO> convertMessageListToMap(List<Message> messages) {
        List<MessageDTO> messageDTOList = new java.util.ArrayList<>();

        for(int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            MessageDTO messageDTO = new MessageDTO(message);

            // if the message is a reply to another message, then add the replied message info
            if (message.getRepliedMessageId() != null) {
                RepliedMessage repliedMessage = findRepliedMessageInfo(message.getRepliedMessageId(), messages);
                messageDTO.setRepliedMessage(repliedMessage);
            }

            messageDTOList.add(messageDTO);
        }

        return messageDTOList;
    }

    private RepliedMessage findRepliedMessageInfo(String repliedMessageId, List<Message> messages) {
        Message message = messages.stream().filter(m -> m.getId().equals(repliedMessageId)).findFirst().orElse(null);

        if (message == null) {
            return null;
        }

        RepliedMessage repliedMessage = new RepliedMessage();
        repliedMessage.setId(message.getId());
        repliedMessage.setMessageContent(message.getContent());

        MemberDTO sender = this.memberMap.get(message.getSenderId());
        repliedMessage.setSenderName(sender.getDisplayName());

        return repliedMessage;

    }

    public ChatUser getTheOtherUserInConversation(ChatUser user, Conversation conversation) {

        // there can be only 2 members in a direct conversation
        // find the other user in the conversation
        ChatUser otherUser = conversation.getMembers().get(0);
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
//        private String username;
        private String avatar;
        private LastSeen lastSeen; // this is the last message seen by the user in the conversation

        public MemberDTO(ChatUser member) {
            this.id = member.getId();
            this.displayName = member.getDisplayName();
            this.avatar = member.getAvatar();
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
        private String messageType;
        private String conversationId;
        private String createdAt;
        private long messageNo;
        private Map<String, Message.Reactor> reactionTracker; // list of user ids who reacted to this message

        private RepliedMessage repliedMessage; // this is the id of the message that this message is replying to

        public MessageDTO(Message message) {
            this.id = message.getId();
            this.senderId = message.getSenderId();
            this.content = message.getContent();
            this.conversationId = message.getConversationId();
            this.messageType = message.getMessageType();
            this.createdAt = message.getCreatedAt().toString();
            this.messageNo = message.getMessageNo();
            this.reactionTracker = message.getReactionTracker();
        }
    }

    @Data
    public static class RepliedMessage {
        private String id;
        private String messageContent;
        private String senderName;

    }



}
