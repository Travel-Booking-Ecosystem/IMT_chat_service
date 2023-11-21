package com.imatalk.chatservice.entity;

import com.imatalk.chatservice.dto.request.UpdateConversationSettingRequest;
import com.imatalk.chatservice.enums.DefaultReaction;
import com.imatalk.chatservice.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;


@Document("conversations")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Conversation {
    @Id
    private String id;
    private LocalDateTime createdAt;

    @DBRef
    private List<ChatUser> members;

    private LastMessage lastMessage; // this is the last message of the conversation used to display in the conversation list
    private LocalDateTime lastUpdatedAt; // this field is used to sort the conversation list when user opens the app

    @Enumerated(EnumType.STRING)
    public ConversationStatus status;

    private Map<String, Long> seenMessageTracker = new HashMap<String, Long>(); // track each user's last seen message number in this conversation


    private Map<String, String> nicknameMap = new HashMap<String, String>(); // map from user id to nickname in this conversation

    private ConversationSetting conversationSetting;



    // it to be false by default if it is not set
    private boolean isGroupConversation = false;
    private String groupName;
    private String groupAvatar;

    public ConversationStatus getStatus() {
        if (status == null) {
            status = ConversationStatus.ACTIVE;
        }

        return status;
    }



    public Map<String, String> getNicknameMap() {
        if (nicknameMap == null) {
            nicknameMap = new HashMap<String, String>();
        }
        return nicknameMap;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class ConversationSetting {
        private String themeColor;
        private String wallpaper;
        private DefaultReaction defaultReaction;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class LastMessage {
        private String id;
        private String content;
        private Long messageNo; // an incremental number for each message in the conversation it belongs to
        private LocalDateTime createdAt;

        public LastMessage(Message message, long lastMessageNo) {
            this.id = message.getId();
            this.content = message.getContent();
            this.createdAt = message.getCreatedAt();
            this.messageNo = lastMessageNo;
        }
    }


    public Map<String, Long> getSeenMessageTracker() {
        if (seenMessageTracker == null) {
            seenMessageTracker = createDefaultSeenMessageTracker(members);
        }
        return seenMessageTracker;
    }

    public static Map<String, Long> createDefaultSeenMessageTracker(List<ChatUser> members) {
        Map<String, Long> map = new HashMap<String, Long>();

        // by default, all members have seen message number 0 (no message seen)
        for (ChatUser member : members) {
            map.put(member.getId(), 0L);
        }

        return map;
    }

    public LocalDateTime getLastUpdatedAt() {
        if (lastUpdatedAt == null) {
            lastUpdatedAt = createdAt;
        }
        return lastUpdatedAt;
    }




    public Long getLastSeenMessageNoOfMember(String memberId) {
        return getSeenMessageTracker().getOrDefault(memberId, 0L);
    }

    public void updateUserSeenLatestMessage(String currentUserId) {

    }

    public List<String> getMemberIds() {
        List<String> memberIds = new ArrayList<String>();
        for (ChatUser member : members) {
            memberIds.add(member.getId());
        }
        return memberIds;
    }

    public long getLastMessageNo() {
        return lastMessage != null ? lastMessage.getMessageNo() : 0L;
    }
}
