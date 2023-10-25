package com.imatalk.chatservice.entity;

import com.imatalk.chatservice.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString(exclude = {"members", "messageSeenRecord"})
public class Conversation {
    @Id
    private String id;
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "conversations")
    private List<User> members;

    @Embedded
    private LastMessage lastMessage; // this is the last message of the conversation used to display in the conversation list
    private LocalDateTime lastUpdatedAt; // this field is used to sort the conversation list when user opens the app

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<MessageSeen> messageSeenRecord;

    @Enumerated(EnumType.STRING)
    public ConversationStatus status;

    @Transient
    private Map<String, Long> seenMessageTracker; // track each user's last seen message number in this conversation

    // it to be false by default if it is not set
    private boolean isGroupConversation = false;
    private String groupName;
    private String groupAvatar;


    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class LastMessage {
        private String lastMessageId;
        private String lastMessageContent;
        private Long lastMessageNo; // an incremental number for each message in the conversation it belongs to
        private LocalDateTime lastMessageCreatedAt;

        public LastMessage(Message message, long lastMessageNo) {
            this.lastMessageId = message.getId();
            this.lastMessageContent = message.getContent();
            this.lastMessageCreatedAt = message.getCreatedAt();
            this.lastMessageNo = lastMessageNo;
        }
    }

    public List<User> getMembers() {
        if (members == null) {
            members = new ArrayList<>();
        }
        return members;
    }

    public Map<String, Long> getSeenMessageTracker() {
        if (seenMessageTracker == null || messageSeenRecord == null) {
            seenMessageTracker = new HashMap<>();

            for (MessageSeen messageSeen : messageSeenRecord) {
                seenMessageTracker.put(messageSeen.getUserId(), messageSeen.getLastSeenMessageNo());
            }
        }
        return seenMessageTracker;
    }

    public static Map<String, Long> createDefaultSeenMessageTracker(List<User> users) {
        Map<String, Long> map = new HashMap<String, Long>();

        // by default, all members have seen message number 0 (no message seen)
        for (User member : users) {
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

    public List<MessageSeen> getMessageSeenRecord() {
        if (messageSeenRecord == null) {
            messageSeenRecord = new ArrayList<>();
        }

        return messageSeenRecord;
    }

    public long getLastMessageNo() {
        return lastMessage != null ? lastMessage.getLastMessageNo() : 0L;
    }
}
